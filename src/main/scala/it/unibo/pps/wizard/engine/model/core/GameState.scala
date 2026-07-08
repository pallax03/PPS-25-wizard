package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.*

sealed trait GameState

object GameState:
  case class ChoosingTrump(
    core: CoreState,
  ) extends GameState
  case class Bidding(core: CoreState, currentBids: Bids, currentPlayer: PlayerId)
      extends GameState
  case class Playing(
      core: CoreState,
      bids: Bids,
      table: Table,
      currentPlayerTurn: PlayerId,
      tricksWon: Tricks
  ) extends GameState
  case class Ended(players: Players, scoreboard: Scoreboard) extends GameState
