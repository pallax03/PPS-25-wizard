package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*

trait ScoringRules:
  def processScores(players: List[Player], bids: BidsCollection, tricksWon: TricksWon, currentScoreboard: Scoreboard): Scoreboard

object ScoringRules:

  def apply(): ScoringRules = new StandardWizardScoringRules

  private class StandardWizardScoringRules extends ScoringRules:

    // Official Wizard rules scoring constants
    private final val BASE_WIN_POINTS = 20
    private final val POINTS_PER_TRICK = 10
    private final val PENALTY_PER_TRICK_DIFF = 10

    override def processScores(players: List[Player], bids: BidsCollection, tricksWon: TricksWon, currentScoreboard: Scoreboard): Scoreboard =
      players.foldLeft(currentScoreboard) { (sb, player) =>
        val playerId = player.id
        val bid = bids.getBid(playerId).getOrElse(Bid.zero)
        val won = tricksWon.getTricks(playerId)
        val roundPoints = calculatePlayerScore(bid, won)

        sb.updateScore(playerId, roundPoints)
      }

    private def calculatePlayerScore(bid: Bid, won: Int): Int = {
      val bidVal = bid.toInt
      if bidVal == won then
        BASE_WIN_POINTS + (won * POINTS_PER_TRICK)
      else
        val difference = Math.abs(bidVal - won)
        -(difference * PENALTY_PER_TRICK_DIFF)
    }