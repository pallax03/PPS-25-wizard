package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.Bid._
import it.unibo.pps.wizard.engine.model.basic._
import it.unibo.pps.wizard.engine.model.basic.gameplay.Round
import it.unibo.pps.wizard.engine.model.core.GameError

object BiddingRules:

  def processBid(
      bid: Bid,
      currentBids: Bids,
      currentPlayer: PlayerId,
      round: Round,
      totalPlayers: Int
  ): Either[GameError, Bids] =
    bid
      .validateBid(round, currentBids, totalPlayers)
      .map(_ => currentBids + (currentPlayer -> bid))

  extension (bid: Bid)
    def validateBid(round: Round, currentBids: Bids, totalPlayers: Int): Either[GameError, Unit] =
      if !bid.isWithinBounds(round) then Left(GameError.InvalidBid)
      else if bid.isLastPlayerInvalid(round, currentBids, totalPlayers) then
        Left(GameError.InvalidBid)
      else Right(())

    private def isWithinBounds(round: Round): Boolean =
      bid >= Bid(0) && bid.isValid(round)

    private def isLastPlayerInvalid(round: Round, currentBids: Bids, totalPlayers: Int): Boolean =
      val isLastPlayer = currentBids.isComplete(totalPlayers - 1)
      isLastPlayer && (currentBids.total + bid) == round.value
export BiddingRules.*
