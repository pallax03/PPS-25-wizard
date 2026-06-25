package it.unibo.pps.wizard.engine.model.game

import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageConsumer
import it.unibo.pps.wizard.engine.events.Event
import it.unibo.pps.wizard.engine.events.Event.addressOf
import it.unibo.pps.wizard.engine.events.WizardEvent.{ActionFailed, GameStarted}
import it.unibo.pps.wizard.engine.model.basic.Players
import it.unibo.pps.wizard.engine.model.configuration.GameConfiguration
import it.unibo.pps.wizard.engine.model.core.GameAction
import it.unibo.pps.wizard.engine.ports.WizardPort
import it.unibo.pps.wizard.util.vertx.VerticleExecutor
import it.unibo.pps.wizard.engine.model.core.GameEngine
import it.unibo.pps.wizard.util.Id

import scala.concurrent.Future
import scala.reflect.ClassTag

class WizardGame(private val vertx: Vertx) extends WizardPort:
  private var state: WizardGameState = WizardGameState.NotConfigured
  private val verticleExecutor: VerticleExecutor = VerticleExecutor(this.vertx)
  private var subscriptions: Map[String, MessageConsumer[?]] = Map.empty

  override def getState: Future[WizardGameState] =
    runOnVerticle("State Retrieval"):
      this.state

  override def startGame(players: Players, config: GameConfiguration): Future[Unit] =
    runOnVerticle("Game Start"):
      this.state match
        case WizardGameState.NotConfigured =>
          val initialState = GameEngine.initializeGame(Players.create(players, config.numberOfBots))
          this.state = WizardGameState.Running(initialState)
          this.publish(GameStarted(initialState))
        case _ =>

  override def submitAction(action: GameAction): Future[Unit] =
    runOnVerticle("Action Submission"):
      this.state match
        case WizardGameState.Running(state) =>
          GameEngine.processAction(state, action) match
            case Left(error) =>
              println(s"Error processing action: $error")
              this.publish(ActionFailed(action.playerId, error.toString))
            case Right(newState) =>
              this.state = WizardGameState.Running(newState)
              this.publish(GameStarted(newState))
        case _ =>

  override def subscribe[T <: Event : ClassTag](handler: T => Unit): Future[String] =
    val subscriptionId: String = Id()
    runOnVerticle(s"Subscription to ${addressOf[T]} {#${subscriptionId}}"):
      this.subscriptions +=
        subscriptionId ->
          this.vertx
            .eventBus()
            .consumer[T](addressOf[T], message => handler(message.body))
      subscriptionId

  override def unsubscribe(subscriptionIds: String*): Future[Unit] =
    runOnVerticle(s"Unsubscription from ${subscriptionIds.mkString(", ")}"):
      subscriptionIds.foreach: subscriptionId =>
        this.subscriptions.get(subscriptionId).foreach: consumer =>
          consumer.unregister()
          this.subscriptions -= subscriptionId


  private def publish[T <: Event : ClassTag](event: T): Unit =
    println(s"Publishing event: $event")
    this.vertx.eventBus().publish(addressOf[T], event)

  private def runOnVerticle[T](activityName: String)(activity: => T): Future[T] =
    this.verticleExecutor.runLater:
      println(s"Running activity '$activityName' on verticle...")
      activity
