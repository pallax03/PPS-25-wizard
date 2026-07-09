package it.unibo.pps.wizard.application.bot.strategy

import io.vertx.core.Vertx
import it.unibo.pps.wizard.engine.events.InvitationEvent
import it.unibo.pps.wizard.engine.model.configuration.BotsDifficulty
import it.unibo.pps.wizard.engine.model.core.GameAction
import it.unibo.pps.wizard.engine.ports.WizardAIPort

import scala.concurrent.Future

trait BotStrategy:
  def decide(invitation: InvitationEvent): Future[GameAction]


object BotStrategy:
  def apply(difficulty: BotsDifficulty): (WizardAIPort, Vertx) => BotStrategy = difficulty match
    case BotsDifficulty.Dumb   => (_, vertx) => new DumbBotStrategy(vertx)
    case BotsDifficulty.Prolog => (aiPort, _) => new PrologBotStrategy(aiPort)