package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*


case class CoreState(
                      players: List[Player],
                      deck: Deck,
                      round: Round,
                      dealerId: PlayerId
                    )

sealed trait GameState
object GameState:
  case class Dealing(core: CoreState) extends GameState
  case class Bidding(core: CoreState,
                     trump: Option[Trump],
                     currentBids: BidsCollection,
                     currentPlayer: PlayerId) extends GameState
  case class Playing(
                      core: CoreState,
                      trump: Option[Trump],
                      bids: BidsCollection,
                      table: Table,
                      currentPlayerTurn: PlayerId,
                      tricksWon: TricksWon,
                      leadPlayer: PlayerId
                    ) extends GameState
  case class Scoring(core: CoreState,
                     bids: BidsCollection,
                     bidsWon: BidsCollection) extends GameState

sealed trait GameAction

object GameAction:
  case class PlaceBid(playerId: PlayerId, bid: Bid) extends GameAction
  case class chooseTrump(playerId: PlayerId, color: Card.Color) extends GameAction
  case class PlayCard(playerId: PlayerId, card: Card) extends GameAction

enum GameError:
  case NotYourTurn
  case InvalidBid
  case CardNotAllowed
  case InvalidAction