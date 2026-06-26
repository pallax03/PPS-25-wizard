package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.*

/**
 * Represents the pure, immutable state of the game at any given moment.
 * * The GameState is responsible for storing the essential facts and behaviors of
 * the ongoing match (e.g., players, cards on the table, current phase).
 *
 * Dealing: The initial phase where cards are dealt to players based on the current round number.
 * Bidding: The phase where each player, starting with the one after the dealer, declares how many tricks they expect to win. (expecting tricks are called bids)
 * Playing: The active gameplay phase where players play their cards to complete tricks.
 * Scoring: The final phase of the round where scores are calculated based on the bids made and the tricks actually won.
 */
sealed trait GameState

object GameState:
  case class Dealing(core: CoreState) extends GameState
  case class Bidding(core: CoreState,
                     trump: Trump,
                     currentBids: BidsCollection,
                     currentPlayer: PlayerId) extends GameState
  case class Playing(
                      core: CoreState,
                      trump: Trump,
                      bids: BidsCollection,
                      table: Table,
                      currentPlayerTurn: PlayerId,
                      tricksWon: TricksWon,
                    ) extends GameState
  case class Scoring(core: CoreState,
                     bids: BidsCollection,
                     bidsWon: BidsCollection) extends GameState