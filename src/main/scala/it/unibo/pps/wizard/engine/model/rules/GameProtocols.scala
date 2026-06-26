package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*


case class CoreState(
                      players: List[Player],
                      hands: Hands,
                      deck: Deck,
                      round: Round,
                      dealerId: PlayerId,
                      scoreboard: Scoreboard
                    )

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

sealed trait GameAction

object GameAction:
  case class PlaceBid(playerId: PlayerId, bid: Bid) extends GameAction
  case class ChooseTrump(playerId: PlayerId, color: Card.Color) extends GameAction
  case class PlayCard(playerId: PlayerId, card: Card) extends GameAction

enum GameError:
  case NotYourTurn
  case InvalidBid
  case CardNotAllowed
  case InvalidAction
  case PlayerNotFound