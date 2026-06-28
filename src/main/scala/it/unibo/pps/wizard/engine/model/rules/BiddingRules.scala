package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameError

object BiddingRules:

  def processBid(bid: Bid, currentBids: Bids, currentPlayer: PlayerId, round: Round, totalPlayers: Int): Either[GameError, Bids] =
    bid.validateBid(round, currentBids, totalPlayers)
      .map(_ => currentBids + (currentPlayer -> bid))

  extension (bid: Bid)
    def validateBid(round: Round, currentBids: Bids, totalPlayers: Int): Either[GameError, Unit] =
      if !bid.isWithinBounds(round) then
        Left(GameError.InvalidBid)
      else if bid.isLastPlayerInvalid(round, currentBids, totalPlayers) then
        Left(GameError.InvalidBid)
      else
        Right(())

    private def isWithinBounds(round: Round): Boolean =
      bid >= Bid.zero && bid.isValid(round)

    private def isLastPlayerInvalid(round: Round, currentBids: Bids, totalPlayers: Int): Boolean =
      val isLastPlayer = currentBids.isComplete(totalPlayers - 1)
      isLastPlayer && (currentBids.total + bid) == round

//  DSL SE BiddingRules OPERASSE SOLO DA VALIDATORE E LASCIASSE LA LOGICA DI AGGIORNAMENTO DEI BIDS A ROUND MANAGER, SI POTREBBE FARE COSI:
//      extension (bid: Bid)
//        def validate(round: Round, bids: Bids, totalPlayers: Int): Either[GameError, Unit] =
//          if bid.isInvalidBounds(round) then Left(GameError.InvalidBid)
//          else if bid.isForbiddenTotal(round, bids, totalPlayers) then Left(GameError.InvalidBid)
//          else Right(())
//
//        private def isInvalidBounds(round: Round): Boolean =
//          !(bid >= Bid.zero && bid.isValid(round))
//
//        private def isForbiddenTotal(round: Round, bids: Bids, totalPlayers: Int): Boolean =
//          val isLastPlayer = bids.isComplete(totalPlayers - 1)
//          isLastPlayer && (bids.total + bid) == round