package it.unibo.pps.wizard.model

opaque type Trump = (Card, Option[Card.Color])
opaque type Round = Int
opaque type Bid = Int
opaque type BidsCollection = Map[PlayerId, Bid]
opaque type Table = List[(PlayerId, Card)] // first card of Table is the 'leaderColor' -> transform to Table Object
opaque type Scoreboard = Map[PlayerId, Int]
opaque type TricksWon = Map[PlayerId, Int]

object Bid:
  def apply(value: Int): Bid = value

  extension (b: Bid)
    def toInt: Int = b
    def >= (other: Bid): Boolean = b >= other
    def <= (other: Bid): Boolean = b <= other

object Round:
  def apply(value: Int): Round = value

  extension (r: Round)
    def toInt: Int = r
    def next: Round = Round(r + 1)

object BidsCollection:
  def empty: BidsCollection = Map.empty

  extension (bc: BidsCollection)
    def +(elem: (PlayerId, Bid)): BidsCollection = bc + elem
    def size: Int = bc.size
    def getBid(p: PlayerId): Option[Bid] = bc.get(p)

object TricksWon:
  def initialize(players: List[Player]): TricksWon =
    players.map(_.id -> 0).toMap

  extension (tw: TricksWon)
    def getTricks(p: PlayerId): Int = tw.getOrElse(p, 0)

object Scoreboard:
  def empty: Scoreboard = Map.empty

  extension (sb: Scoreboard)
    def getPoints(p: PlayerId): Int = sb.getOrElse(p, 0)
    // Aggiorna il punteggio sommando i punti del round corrente a quelli passati
    def updateScore(p: PlayerId, roundPoints: Int): Scoreboard =
      sb.updated(p, sb.getPoints(p) + roundPoints)
  
object Table:
  def empty: Table = List.empty
  
case class CoreState(
                      players: List[Player],
                      deck: Deck,
                      round: Round,
                      dealerId: PlayerId
                    )

sealed trait GameState

object GameState:
  case class Dealing(core: CoreState) extends GameState

  case class Bidding(core: CoreState, trump: Option[Trump], currentBids: BidsCollection, currentPlayer: PlayerId) extends GameState

  case class Playing(
                      core: CoreState,
                      trump: Option[Trump],
                      bids: BidsCollection,
                      table: Table,
                      currentPlayerTurn: PlayerId,
                      tricksWon: TricksWon,
                      leadPlayer: PlayerId
                    ) extends GameState

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