package it.unibo.pps.wizard.application.bot

import io.vertx.core.AbstractVerticle
import it.unibo.pps.wizard.application.bot.strategy.{BotStrategy, DumbBotStrategy}
import it.unibo.pps.wizard.engine.events.*
import it.unibo.pps.wizard.engine.model.basic.{PlayerId, Players}
import it.unibo.pps.wizard.engine.model.core.GameAction
import it.unibo.pps.wizard.engine.ports.WizardPort

class BotManagerVerticle(
    wizardPort: WizardPort,
    strategyFactory: PlayerId => BotStrategy = _ => DumbBotStrategy()
) extends AbstractVerticle:
  private var bots: Map[PlayerId, BotStrategy] = Map.empty

  override def start(): Unit =
    wizardPort.subscribe[LifecycleEvent]:
      case LifecycleEvent.GameStarted(players) => registerBots(players)
      case _: LifecycleEvent.GameEnded              => bots = Map.empty

    wizardPort.subscribe[InvitationEvent]: invitation =>
      bots
        .get(invitation.playerId)
        .foreach: strategy =>
          submit(strategy.decide(invitation))

  private def registerBots(players: Players): Unit =
    bots = players.toList
      .filter(_.isBot)
      .map(player => player.id -> strategyFactory(player.id))
      .toMap

  private def submit(action: GameAction): Unit = wizardPort.submitAction(action)
