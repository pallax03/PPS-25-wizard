package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*

object ScoringRules:
  // Official Wizard rules scoring constants
  private final val BASE_WIN_POINTS = 20
  private final val POINTS_PER_TRICK = 10

  def compute(
      players: Players,
      bids: Bids,
      tricks: Tricks,
      round: Round,
      scoreboard: Scoreboard
  ): Scoreboard =
    players.toList.foldLeft(scoreboard): (sb, player) =>
      val bid = bids(player.id)
      val tricksWon = tricks(player.id)
      val roundPoints = bid.calculatePointsFor(tricksWon)

      val previousRoundNum = round.value - 1
      val previousScore =
        if previousRoundNum > 0 then sb.getStatsForRound(Round(previousRoundNum), player.id)._1
        else Score.zero

      val cumulativePoints = Score(previousScore.value + roundPoints.value)

      sb.addScore(player.id, round, cumulativePoints, bid)

  extension (bid: Bid)
    def calculatePointsFor(tricksWon: Trick): Score =
      val points =
        if bid == tricksWon
        then BASE_WIN_POINTS + (tricksWon * POINTS_PER_TRICK)
        else -Math.abs(bid - tricksWon) * POINTS_PER_TRICK
      Score(points)

export ScoringRules.*
