package it.unibo.pps.wizard.engine.model.basic

opaque type Scoreboard = Map[PlayerId, Int]

object Scoreboard:
  def empty: Scoreboard = Map.empty

  extension (scoreboard: Scoreboard)
    def getScore(playerId: PlayerId): Int =
      scoreboard.getOrElse(playerId, 0)

    def updateScore(playerId: PlayerId, score: Int): Scoreboard =
      scoreboard.updated(playerId, scoreboard.getOrElse(playerId, 0) + score)

    def resetScores(): Scoreboard =
      scoreboard.map { case (playerId, _) => (playerId, 0) }