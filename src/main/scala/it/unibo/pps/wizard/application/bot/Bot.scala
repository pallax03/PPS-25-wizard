package it.unibo.pps.wizard.application.bot

import it.unibo.pps.wizard.engine.events.InvitationEvent
import it.unibo.pps.wizard.engine.model.basic.{Bid, Card}
import it.unibo.pps.wizard.engine.model.core.GameAction

import scala.util.Random

trait BotStrategy:
  def decide(invitation: InvitationEvent): GameAction

class DumbBotStrategy(random: Random = Random()) extends BotStrategy:

  override def decide(invitation: InvitationEvent): GameAction = invitation match
    case InvitationEvent.WaitingForBid(context) =>
      GameAction.PlaceBid(context.playerId, Bid.zero)
    case InvitationEvent.WaitingForCard(context) =>
      GameAction.PlayCard(context.playerId, context.legalCards.head)
    case InvitationEvent.WaitingForTrump(context) =>
      val colors = Card.Color.values
      GameAction.ChooseTrump(context.playerId, colors(random.nextInt(colors.length)))
