package it.unibo.pps.wizard.application.bot

import io.vertx.core.AbstractVerticle
import it.unibo.pps.wizard.application.bot.strategy.BotStrategy
import it.unibo.pps.wizard.engine.events.{FailureEvent, InvitationEvent, LifecycleEvent}
import it.unibo.pps.wizard.engine.model.basic.{PlayerId, Players}
import it.unibo.pps.wizard.engine.model.configuration.BotsDifficulty
import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardInboundPort}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Failure

class BotManagerVerticle(
    wizardInboundPort: WizardInboundPort,
    wizardAIPort: WizardAIPort
) extends AbstractVerticle:

  private var bots: Map[PlayerId, BotStrategy] = Map.empty

  override def start(): Unit =
    wizardInboundPort.subscribe[LifecycleEvent]:
      case LifecycleEvent.GameStarted(players, difficulty) => registerBots(players, difficulty)
      case _: LifecycleEvent.GameEnded                     => bots = Map.empty

    wizardInboundPort.subscribe[InvitationEvent]: invitation =>
      bots
        .get(invitation.playerId)
        .foreach: strategy =>
          strategy
            .resolveInvitationEvents(invitation)
            .andThen:
              case Failure(error) =>
                println(s"Bot ${invitation.playerId} failed on $invitation: ${error.getMessage}")
            .foreach: action =>
              wizardInboundPort.submitAction(action)
    wizardInboundPort.subscribe[FailureEvent]: failure =>
      bots
        .get(failure.playerId)
        .foreach: strategy =>
          strategy
            .resolveFailedEvents(failure)
            .andThen:
              case Failure(error) =>
                println(
                  s"Bot ${failure.playerId} failed to recover from $failure: ${error.getMessage}"
                )
            .foreach: action =>
              wizardInboundPort.submitAction(action)

  private def registerBots(players: Players, difficulty: BotsDifficulty): Unit =
    bots = players.toList
      .filter(_.isBot)
      .map(player => player.id -> BotStrategy(difficulty)(wizardAIPort, vertx))
      .toMap
