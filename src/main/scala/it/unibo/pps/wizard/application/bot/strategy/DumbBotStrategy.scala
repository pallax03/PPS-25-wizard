package it.unibo.pps.wizard.application.bot.strategy

import io.vertx.core.Vertx
import it.unibo.pps.wizard.engine.events.{FailureEvent, InvitationEvent}
import it.unibo.pps.wizard.engine.model.basic.{Bid, Card, Round}
import it.unibo.pps.wizard.engine.model.core.{GameAction, GameError}

import scala.concurrent.{Future, Promise}
import scala.util.Random

class DumbBotStrategy(vertx: Vertx, random: Random = Random()) extends BotStrategy:
  private var bid: (Round, Bid) = Round.start -> Bid(0)

  private def delayed[T](delayMs: Long)(action: => T): Future[T] =
    val promise = Promise[T]()
    vertx.setTimer(delayMs, _ => promise.success(action))
    promise.future

  override def resolveInvitationEvents(invitation: InvitationEvent): Future[GameAction] =
    delayed(1000):
      invitation match
        case InvitationEvent.WaitingForBid(playerId, round) =>
          bid = round -> Bid(random.nextInt(round.value + 1))
          GameAction.PlaceBid(
            playerId,
            bid._2
          )

        case InvitationEvent.WaitingForCard(playerId, legalCards) =>
          GameAction.PlayCard(playerId, legalCards.head)

        case InvitationEvent.WaitingForTrump(playerId) =>
          val colors = Card.Color.values
          GameAction.ResolveTrumpColor(playerId, colors(random.nextInt(colors.length)))

  override def resolveFailedEvents(failure: FailureEvent): Future[GameAction] =
    delayed(500):
      failure match
        case FailureEvent.ActionFailed(playerId, reason) =>
          reason match
            case GameError.InvalidBid =>
              val (round, lastBid) = bid
              bid = round -> Bid((lastBid.value + 1) % (round.value + 1))
              GameAction.PlaceBid(playerId, bid._2)

            case GameError.CardNotAllowed(notAllowedReason) =>
              GameAction.PlayCard(playerId, notAllowedReason.legitCards.head)

            case _ =>
              throw IllegalStateException(s"Dumb bot cannot recover from $reason")
