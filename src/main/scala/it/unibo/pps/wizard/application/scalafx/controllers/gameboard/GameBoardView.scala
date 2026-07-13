package it.unibo.pps.wizard.application.scalafx.controllers.gameboard

import it.unibo.pps.wizard.engine.model.basic.*

trait GameBoardView:
  def displayWaitingForTrump(playerId: PlayerId): Unit
  def displayTrickWon(winnerId: PlayerId, tricksWon: Int, trickedCards: List[Card]): Unit
  def displayPhaseChanged(phase: String): Unit
  def displayCardsDealt(playerId: PlayerId, hands: Hands, trump: Trump, round: Round): Unit
  def displayCardPlayed(playerId: PlayerId, card: Card): Unit
  def displayTrumpSelected(playerId: PlayerId, color: Card.Color): Unit
  def displayBidPlaced(playerId: PlayerId, bid: Bid): Unit
  def displayTurnChanged(playerId: PlayerId, phase: String): Unit
  def displayRoundScored(scoreboard: Scoreboard): Unit
  def displayGameStarted(players: Players): Unit
