package it.unibo.pps.wizard.engine.model.basic

opaque type Bid = Int
object Bid:
  def apply(value: Int): Bid = value

  extension (b: Bid)
    def inc: Bid = b+1
    def value: Int = b

opaque type BidsCollection = Map[PlayerId, Bid]
opaque type TricksWon = BidsCollection
object BidsCollection:
  def empty(players: List[PlayerId]): BidsCollection = players.map(p => p -> Bid.apply(0)).toMap

  extension (b: BidsCollection)
    def getBid(playerId: PlayerId): Option[Bid] = b.get(playerId)