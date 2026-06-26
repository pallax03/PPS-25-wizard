package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameError

trait BiddingRules:
  def processBid(bid: Bid, currentBids: BidsCollection, currentPlayer: PlayerId, round: Round, totalPlayers: Int): Either[GameError, BidsCollection]

object BiddingRules:

  def apply(): BiddingRules = new StandardWizardBiddingRules

  private class StandardWizardBiddingRules extends BiddingRules:

    override def processBid(bid: Bid, currentBids: BidsCollection, currentPlayer: PlayerId, round: Round, totalPlayers: Int): Either[GameError, BidsCollection] =
      if isBidValid(bid, round, currentBids, totalPlayers)
      then
        val updatedBids = currentBids + (currentPlayer -> bid)
        Right(updatedBids)
      else Left(GameError.InvalidBid)

    private def isBidValid(bid: Bid, round: Round, currentBids: BidsCollection, totalPlayers: Int): Boolean =
      val isWithinBounds = bid >= Bid.zero && bid <= round
      val isLastPlayer = currentBids.size == totalPlayers - 1

      val isHookValid = !isLastPlayer || (currentBids.sum + bid) != round

      isWithinBounds && isHookValid