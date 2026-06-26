package it.unibo.pps.wizard.engine.model.basic

opaque type TricksWon = Map[PlayerId, Int]

object TricksWon:
  def apply(tricks: Map[PlayerId, Int]): TricksWon = tricks

  def initialize(players: List[Player]): TricksWon =
    players.map(_.id -> 0).toMap

  extension (tw: TricksWon)
    def getTricks(p: PlayerId): Int = tw.getOrElse(p, 0)