package it.unibo.pps.wizard.engine.model.view

import it.unibo.pps.wizard.engine.model.basic.*

sealed trait InvitationContext:
  def playerId: PlayerId
  def hand: Hand

case class BidContext(
    playerId: PlayerId,
    hand: Hand,
    trump: Trump,
    round: Round,
    currentBids: Bids,
    totalPlayers: Int,
    dealerId: PlayerId
) extends InvitationContext

case class TrumpContext(
    playerId: PlayerId,
    hand: Hand,
    trump: Trump
) extends InvitationContext

case class PlayCardContext(
    playerId: PlayerId,
    hand: Hand,
    legalCards: List[Card],
    table: Table,
    trump: Trump,
    bid: Bid,
    tricksWon: Int,
    currentWinningCard: Option[Card],
    followingColor: Option[Card.Color]
) extends InvitationContext
