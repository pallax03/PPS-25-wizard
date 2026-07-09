package it.unibo.pps.wizard.application.gui.controllers.gameboard

import it.unibo.pps.wizard.engine.model.basic.*

trait GameBoardView:
  def displayWaitingForTrump(playerId: PlayerId): Unit
  def displayTrickWon(winnerId: PlayerId, trickedCards: List[Card]): Unit
  def displayPhaseChanged(phase: String): Unit
  def displayCardsDealt(playerId: PlayerId, hands: Hands, trump: Trump, round: Round): Unit
  def displayCardPlayed(playerId: PlayerId, card: Card): Unit
  def displayTrumpSelected(playerId: PlayerId, color: Card.Color): Unit
  def displayBidPlaced(playerId: PlayerId, bid: Bid): Unit
