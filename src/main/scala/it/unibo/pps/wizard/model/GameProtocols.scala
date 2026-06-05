package it.unibo.pps.wizard.model

opaque type Trump = (Card, Option[Card.Color])
opaque type Round = Int
opaque type Bid = Int
opaque type BidsCollection = Map[PlayerId, Bid]
opaque type Table = List[(PlayerId, Card)] // first card of Table is the 'leaderColor' -> transform to Table Object
opaque type Scoreboard = Map[PlayerId, Int]

case class CoreState(
                      players: List[Player],
                      deck: Deck,
                      round: Round,
                      dealerId: PlayerId
                    )

sealed trait GameState
object GameState:
  case class Dealing(core: CoreState) extends GameState
  case class Bidding(core: CoreState, trump: Option[Trump]) extends GameState
  case class Playing(core: CoreState, trump: Option[Trump], bids: BidsCollection, table: Table, currentPlayerTurn: PlayerId) extends GameState
  case class Scoring(core: CoreState, bids: BidsCollection, bidsWon: BidsCollection) extends GameState

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


// Game Engine Needs to be Implemented and moved to another .scala file.
trait GameEngine:
  def processAction(state: GameState, action: GameAction): Either[GameError, GameState]