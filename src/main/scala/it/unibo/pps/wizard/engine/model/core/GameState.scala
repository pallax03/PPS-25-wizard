package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.*

sealed trait GameState:
  def getPlayers: Players

object GameState:
  case class Bidding(core: CoreState, trump: Trump, currentBids: Bids, currentPlayer: PlayerId)
      extends GameState:
    override def getPlayers: Players = core.players
  case class Playing(
      core: CoreState,
      trump: Trump,
      bids: Bids,
      table: Table,
      currentPlayerTurn: PlayerId,
      tricksWon: Tricks
  ) extends GameState:
    override def getPlayers: Players = core.players
  case class Ended(players: Players, scoreboard: Scoreboard) extends GameState:
    override def getPlayers: Players = players
