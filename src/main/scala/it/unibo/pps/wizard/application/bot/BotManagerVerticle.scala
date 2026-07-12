package it.unibo.pps.wizard.application.bot

import io.vertx.core.AbstractVerticle
import it.unibo.pps.wizard.application.bot.strategy.BotStrategy
import it.unibo.pps.wizard.engine.events.{FailureEvent, InvitationEvent, LifecycleEvent, PlayerScoped}
import it.unibo.pps.wizard.engine.model.basic.{PlayerId, Players}
import it.unibo.pps.wizard.engine.model.configuration.BotsDifficulty
import it.unibo.pps.wizard.engine.model.core.GameAction
import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardInboundPort}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

class BotManagerVerticle(
    wizardInboundPort: WizardInboundPort,
    wizardAIPort: WizardAIPort
) extends AbstractVerticle:

  private var bots: Map[PlayerId, BotStrategy] = Map.empty
  
  override def start(): Unit =
    wizardInboundPort.subscribe[LifecycleEvent]:
      case LifecycleEvent.GameStarted(players, difficulty) => registerBots(players, difficulty)
      case _: LifecycleEvent.GameEnded                     => bots = Map.empty

    subscribeToEvents[InvitationEvent](1000): (strategy, event) => 
      strategy.resolveInvitationEvents(event)
      
    subscribeToEvents[FailureEvent](500): (strategy, event) =>
        strategy.resolveFailedEvents(event)

  private def registerBots(players: Players, difficulty: BotsDifficulty): Unit =
    bots = players.toList
      .filter(_.isBot)
      .map(player => player.id -> BotStrategy(difficulty, wizardAIPort))
      .toMap

  private def delayed[T](delayMs: Long)(action: => Future[T]): Future[T] =
    val promise = Promise[T]()
    vertx.setTimer(delayMs, _ => promise.completeWith(action))
    promise.future

  private def subscribeToEvents[E <: PlayerScoped : ClassTag](delayMs: Long)(resolver: (BotStrategy, E) => Future[GameAction]): Unit =
    wizardInboundPort.subscribe[E]: event =>
      bots.get(event.playerId).foreach: strategy =>
          delayed(delayMs)(resolver(strategy, event)).onComplete:
            case Success(action) =>
              wizardInboundPort.submitAction(action)
            case Failure(error) =>
              println(s"Bot ${event.playerId} failed on $event: ${error.getMessage}")
            
  