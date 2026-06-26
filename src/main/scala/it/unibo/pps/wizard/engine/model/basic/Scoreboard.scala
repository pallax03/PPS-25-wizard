package it.unibo.pps.wizard.engine.model.basic

opaque type Scoreboard = Map[PlayerId, Int]

object Scoreboard:
  def empty: Scoreboard = Map.empty

  extension (sb: Scoreboard)
    def getPoints(p: PlayerId): Int = sb.getOrElse(p, 0)
    def updateScore(p: PlayerId, roundPoints: Int): Scoreboard = 
      sb.updated(p, sb.getPoints(p) + roundPoints)