package it.unibo.pps.wizard.application.bot.strategy

import it.unibo.pps.wizard.application.bot.strategy.BotStrategy
import it.unibo.pps.wizard.engine.events.InvitationEvent
import it.unibo.pps.wizard.engine.model.basic.{Bid, Card, Round}
import it.unibo.pps.wizard.engine.model.core.GameAction

import scala.util.Random

class DumbBotStrategy(random: Random = Random()) extends BotStrategy:

  override def decide(invitation: InvitationEvent): GameAction = invitation match
    case InvitationEvent.WaitingForBid(context) =>
      GameAction.PlaceBid(context.playerId, Bid(random.between(Round.start.value, context.round.value)))
    case InvitationEvent.WaitingForCard(context) =>
      GameAction.PlayCard(context.playerId, context.legalCards.head)
    case InvitationEvent.WaitingForTrump(context) =>
      val colors = Card.Color.values
      GameAction.ResolveTrumpColor(context.playerId, colors(random.nextInt(colors.length)))
