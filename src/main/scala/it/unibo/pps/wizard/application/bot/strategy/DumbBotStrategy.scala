package it.unibo.pps.wizard.application.bot.strategy

import io.vertx.core.Vertx
import it.unibo.pps.wizard.application.bot.strategy.BotStrategy
import it.unibo.pps.wizard.engine.events.InvitationEvent
import it.unibo.pps.wizard.engine.model.basic.{Bid, Card}
import it.unibo.pps.wizard.engine.model.core.GameAction

import scala.concurrent.{Future, Promise}
import scala.util.Random

class DumbBotStrategy(vertx: Vertx, random: Random = Random()) extends BotStrategy:
  override def decide(invitation: InvitationEvent): Future[GameAction] =
    val promise = Promise[GameAction]()
    vertx.setTimer(1000, _ => promise.success(dumbDecide(invitation)))
    promise.future

  private def dumbDecide(invitation: InvitationEvent): GameAction = invitation match
    case InvitationEvent.WaitingForBid(context) =>
      GameAction.PlaceBid(context.playerId, Bid(1))//random.between(Round.start.value, context.round.value)))
    case InvitationEvent.WaitingForCard(context) =>
      GameAction.PlayCard(context.playerId, context.legalCards.head)
    case InvitationEvent.WaitingForTrump(context) =>
      val colors = Card.Color.values
      GameAction.ResolveTrumpColor(context.playerId, colors(random.nextInt(colors.length)))