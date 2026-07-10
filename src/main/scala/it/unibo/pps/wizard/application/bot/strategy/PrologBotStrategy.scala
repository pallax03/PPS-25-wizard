package it.unibo.pps.wizard.application.bot.strategy

import it.unibo.pps.wizard.application.bot.strategy.BotStrategy
import it.unibo.pps.wizard.engine.events.InvitationEvent
import it.unibo.pps.wizard.engine.model.basic.{Bid, Card}
import it.unibo.pps.wizard.engine.model.core.GameAction
import it.unibo.pps.wizard.engine.ports.WizardAIPort

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PrologBotStrategy(port: WizardAIPort) extends BotStrategy:

  override def decide(invitation: InvitationEvent): Future[GameAction] = invitation match
    case InvitationEvent.WaitingForCard(context) =>
      port.getBestCard(context.playerId).map(card => GameAction.PlayCard(context.playerId, card))
    case InvitationEvent.WaitingForBid(context) =>
      Future.successful(GameAction.PlaceBid(context.playerId, Bid.zero))
    case InvitationEvent.WaitingForTrump(context) =>
      Future.successful(GameAction.ResolveTrumpColor(context.playerId, Card.Color.values.head))
