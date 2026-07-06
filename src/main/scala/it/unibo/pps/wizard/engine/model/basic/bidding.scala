package it.unibo.pps.wizard.engine.model.basic

opaque type Bid = Int

object Bid:
  def apply(value: Int): Bid = value

  val zero: Bid = apply(0)

  extension (b: Bid)
    def value: Int = b
    infix def +(other: Bid): Bid = b + other
    def >=(other: Bid): Boolean = (b: Int) >= (other: Int)
    def isValid(round: Round): Boolean = b <= round.value

opaque type Bids = Map[PlayerId, Bid]

object Bids:
  def empty: Bids = Map.empty

  extension (b: Bids)
    def apply(p: PlayerId): Bid = b.getOrElse(p, Bid.zero)
    infix def +(entry: (PlayerId, Bid)): Bids = b + entry
    def isComplete(totalPlayers: Int): Boolean = b.size == totalPlayers
    def total: Bid = b.values.foldLeft(Bid.zero)(_ + _)
    def size: Int = b.size

opaque type Tricks = Bids

object Tricks:
  def apply(tricks: Map[PlayerId, Int]): Tricks = tricks
  def initialize(players: List[Player]): Tricks =
    players.map(_.id -> 0).toMap
  def empty: Tricks = Map.empty

  extension (t: Tricks)
    def apply(p: PlayerId): Int = t.getOrElse(p, 0)
    def addTrickTo(p: PlayerId): Tricks = t.updated(p, t(p) + 1)
    def total: Int = t.values.sum
