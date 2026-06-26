package it.unibo.pps.wizard.engine.model.basic

opaque type Bid = Int
object Bid:
  def apply(value: Int): Bid = value

  val zero: Bid = apply(0)

  extension (b: Bid)
    def value: Int = b
    def +(other: Bid): Bid = b + other
    def >=(other: Bid): Boolean = (b: Int) >= (other: Int)
    def <=(round: Round): Boolean = b <= round.toInt

opaque type BidsCollection = Map[PlayerId, Bid]
object BidsCollection:
  def empty: BidsCollection = Map.empty

  extension (bc: BidsCollection)
    def +(elem: (PlayerId, Bid)): BidsCollection = bc + elem
    def size: Int = bc.size
    def getBid(p: PlayerId): Option[Bid] = bc.get(p)
    def sum: Bid = bc.values.foldLeft(Bid(0))(_ + _)


opaque type TricksWon = BidsCollection
object TricksWon:
  def apply(tricks: Map[PlayerId, Int]): TricksWon = tricks

  def initialize(players: List[Player]): TricksWon =
    players.map(_.id -> 0).toMap

  extension (tw: TricksWon)
    def getTricks(p: PlayerId): Int = tw.getOrElse(p, 0)