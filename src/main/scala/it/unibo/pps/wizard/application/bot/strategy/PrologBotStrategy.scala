package it.unibo.pps.wizard.application.bot.strategy

import it.unibo.pps.wizard.application.bot.strategy.BotStrategy
import it.unibo.pps.wizard.engine.events.InvitationEvent
import it.unibo.pps.wizard.engine.model.core.GameAction
import it.unibo.pps.wizard.engine.ports.WizardAIPort

import scala.concurrent.Future

class PrologBotStrategy(port: WizardAIPort) extends BotStrategy:

  override def decide(invitation: InvitationEvent): Future[GameAction] = invitation match
    case InvitationEvent.WaitingForCard(context) => ???
    case InvitationEvent.WaitingForBid(context) => ???
    case InvitationEvent.WaitingForTrump(context) => ???