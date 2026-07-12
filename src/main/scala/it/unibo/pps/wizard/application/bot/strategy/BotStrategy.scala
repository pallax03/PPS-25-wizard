package it.unibo.pps.wizard.application.bot.strategy

import it.unibo.pps.wizard.engine.events.{FailureEvent, InvitationEvent}
import it.unibo.pps.wizard.engine.model.configuration.BotsDifficulty
import it.unibo.pps.wizard.engine.model.core.GameAction
import it.unibo.pps.wizard.engine.ports.WizardAIPort

import scala.concurrent.Future

trait BotStrategy:
  def resolveInvitationEvents(invitation: InvitationEvent): Future[GameAction]
  def resolveFailedEvents(failure: FailureEvent): Future[GameAction]

object BotStrategy:
  def apply(difficulty: BotsDifficulty, wizardAIPort: WizardAIPort): BotStrategy = difficulty match
    case BotsDifficulty.Dumb   => new DumbBotStrategy()
    case BotsDifficulty.Prolog => new PrologBotStrategy(wizardAIPort)
