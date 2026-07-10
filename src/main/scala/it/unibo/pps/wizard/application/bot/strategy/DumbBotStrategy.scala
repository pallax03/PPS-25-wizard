package it.unibo.pps.wizard.application.bot.strategy

import io.vertx.core.Vertx
import it.unibo.pps.wizard.application.bot.strategy.BotStrategy
import it.unibo.pps.wizard.engine.events.{FailureEvent, InvitationEvent}
import it.unibo.pps.wizard.engine.model.basic.{Bid, Card}
import it.unibo.pps.wizard.engine.model.core.{GameAction, GameError}

import scala.concurrent.{Future, Promise}
import scala.util.Random

class DumbBotStrategy(vertx: Vertx, random: Random = Random()) extends BotStrategy:
  override def resolveInvitationEvents(invitation: InvitationEvent): Future[GameAction] =
    val promise = Promise[GameAction]()
    vertx.setTimer(1000, _ => promise.success(dumbResolver(invitation)))
    promise.future

  private def dumbResolver(invitation: InvitationEvent): GameAction = invitation match
    case InvitationEvent.WaitingForBid(playerId, _) =>
      GameAction.PlaceBid(playerId, Bid(1))//random.between(Round.start.value, context.round.value)))
    case InvitationEvent.WaitingForCard(playerId, context) =>
      GameAction.PlayCard(playerId, context.legalCards.head)
    case InvitationEvent.WaitingForTrump(playerId, _) =>
      val colors = Card.Color.values
      GameAction.ResolveTrumpColor(playerId, colors(random.nextInt(colors.length)))

  override def resolveFailedEvents(failure: FailureEvent): Future[GameAction] = failure match
    case FailureEvent.ActionFailed(playerId, reason) => reason match
      case GameError.InvalidBid => ???
      case GameError.CardNotAllowed(reason) => ???
      case _ => ???