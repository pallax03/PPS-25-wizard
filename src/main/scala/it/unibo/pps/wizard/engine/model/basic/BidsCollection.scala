package it.unibo.pps.wizard.engine.model.basic

opaque type BidsCollection = Map[PlayerId, Bid]

object BidsCollection:
  def empty: BidsCollection = Map.empty

  extension (bc: BidsCollection)
    def +(elem: (PlayerId, Bid)): BidsCollection = bc + elem
    def size: Int = bc.size
    def getBid(p: PlayerId): Option[Bid] = bc.get(p)
    def sum: Bid = bc.values.foldLeft(Bid(0))(_ + _)