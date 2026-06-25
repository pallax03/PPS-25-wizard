package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.rules.*

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