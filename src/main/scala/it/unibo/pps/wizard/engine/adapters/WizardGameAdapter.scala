package it.unibo.pps.wizard.engine.adapters

import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageConsumer
import it.unibo.pps.wizard.engine.events.*
import it.unibo.pps.wizard.engine.events.FailureEvent.ActionFailed
import it.unibo.pps.wizard.engine.events.LifecycleEvent.GameStarted
import it.unibo.pps.wizard.engine.model.basic.Players
import it.unibo.pps.wizard.engine.model.configuration.GameConfiguration
import it.unibo.pps.wizard.engine.model.core.{GameAction, GameEngine, GameState}
import it.unibo.pps.wizard.engine.model.view.InvitationContextFactory
import it.unibo.pps.wizard.engine.ports.{WizardInboundPort, WizardOutboundPort}
import it.unibo.pps.wizard.util.{Id, VerticleExecutor}

import scala.concurrent.Future
import scala.reflect.ClassTag

enum WizardGameState:
  case NotConfigured
  case Running(state: GameState)
  case Finished

class WizardGameAdapter(private val vertx: Vertx, private val outboundPort: WizardOutboundPort)
    extends WizardInboundPort:
  private var currentState: WizardGameState = WizardGameState.NotConfigured
  private val verticleExecutor: VerticleExecutor = VerticleExecutor(this.vertx)
  private var subscriptions: Map[String, MessageConsumer[?]] = Map.empty

  override def getState: Future[WizardGameState] =
    runOnVerticle("State Retrieval"):
      this.currentState

  override def startGame(players: Players, config: GameConfiguration): Future[Unit] =
    runOnVerticle("Game Start"):
      this.currentState match
        case WizardGameState.NotConfigured =>
          val playersAndBots: Players = Players.create(players, config.numberOfBots)
          val initialState = GameEngine.initializeGame(playersAndBots)
          this.currentState = WizardGameState.Running(initialState)
          this.outboundPort.publishEvent(GameStarted(playersAndBots, config.botsDifficulty))
          this.publishInvitationEvent(initialState)
        case _ =>

  override def submitAction(action: GameAction): Future[Unit] =
    runOnVerticle("Action Submission"):
      this.currentState match
        case WizardGameState.Running(oldState) =>
          GameEngine.processAction(oldState, action) match
            case Left(error) =>
              println(s"Error processing action: $error")
              this.outboundPort.publishEvent(ActionFailed(action.playerId, error))
            case Right(newState) =>
              this.currentState = WizardGameState.Running(newState)
              val actionEvent = ActionEvent.from(action)
              val progressEvents = ProgressEvent.fromTransition(oldState, newState, action)
              this.outboundPort.publishAllEvents(actionEvent +: progressEvents)
              this.publishInvitationEvent(newState)
        case _ =>

  override def subscribe[T <: Event: ClassTag](handler: T => Unit): Future[String] =
    val subscriptionId: String = Id()
    runOnVerticle(s"Subscription to ${addressOf[T]} {#$subscriptionId}"):
      this.subscriptions +=
        subscriptionId ->
          this.vertx
            .eventBus()
            .consumer[T](addressOf[T], message => handler(message.body))
      subscriptionId

  override def unsubscribe(subscriptionIds: String*): Future[Unit] =
    runOnVerticle(s"Unsubscription from ${subscriptionIds.mkString(", ")}"):
      subscriptionIds.foreach: subscriptionId =>
        this.subscriptions
          .get(subscriptionId)
          .foreach: consumer =>
            consumer.unregister()
            this.subscriptions -= subscriptionId

  private def runOnVerticle[T](activityName: String)(activity: => T): Future[T] =
    this.verticleExecutor.runLater:
      println(s"Running activity '$activityName' on verticle...")
      activity

  private def publishInvitationEvent(state: GameState): Unit =
    InvitationContextFactory.fromState(state).foreach(this.outboundPort.publishEvent)
