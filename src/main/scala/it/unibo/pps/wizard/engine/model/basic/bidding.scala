package it.unibo.pps.wizard.engine.model.basic

type Bid = Int

object Bid:
  def apply(value: Int): Bid = value

  extension (b: Bid) def isValid(round: Round): Boolean = b <= round.value

opaque type Bids = Map[PlayerId, Bid]

object Bids:
  def empty: Bids = Map.empty

  extension (b: Bids)
    def apply(p: PlayerId): Bid = b.getOrElse(p, 0)
    infix def +(entry: (PlayerId, Bid)): Bids = b + entry
    def isComplete(totalPlayers: Int): Boolean = b.size == totalPlayers
    def total: Bid = b.values.sum

type Trick = Int

object Trick:
  def apply(value: Int): Trick = value

  extension (t: Trick) def value: Int = t

opaque type Tricks = Map[PlayerId, Trick]

object Tricks:
  def initialize(players: Players): Tricks =
    players.toList.map(_.id -> 0).toMap
  def empty: Tricks = Map.empty

  extension (t: Tricks)
    def apply(p: PlayerId): Trick = t.getOrElse(p, 0)
    def addTrickTo(p: PlayerId): Tricks = t.updated(p, t.getOrElse(p, 0) + 1)
