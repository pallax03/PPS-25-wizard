package it.unibo.pps.wizard.engine.model.basic.scoreboard

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.basic.gameplay.Round
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestScoreboard extends AnyWordSpec with Matchers:
  import Scoreboard.*

  val p1: PlayerId = PlayerId(1)
  val p2: PlayerId = PlayerId(2)
  val r1: Round = Round(1)
  val r2: Round = Round(2)
  val b1: Bid = Bid(1)
  val b2: Bid = Bid(2)

  "A Scoreboard" when:
    "empty" should:
      val sb = Scoreboard.empty

      "return an empty history map for any player" in:
        sb(p1) shouldBe Map.empty
        sb(p2) shouldBe Map.empty

      "return default stats (0 points, 0 bid) for any unplayed round" in:
        val (score, bid) = sb.getStatsForRound(r1, p1)
        score.value shouldBe 0
        bid shouldBe 0

    "updated with addScore" should:
      "correctly store score and bid for a player in a specific round" in:
        val sb = Scoreboard.empty.addScore(p1, r1, Score(20), b1)
        val (score, bid) = sb.getStatsForRound(r1, p1)

        score.value shouldBe 20
        bid shouldBe 1

      "store distinct stats across multiple rounds for the same player" in:
        val sb = Scoreboard.empty
          .addScore(p1, r1, Score(20), b1)
          .addScore(p1, r2, Score(10), b2)

        val (scoreR1, bidR1) = sb.getStatsForRound(r1, p1)
        scoreR1.value shouldBe 20
        bidR1 shouldBe 1

        val (scoreR2, bidR2) = sb.getStatsForRound(r2, p1)
        scoreR2.value shouldBe 10
        bidR2 shouldBe 2

      "maintain distinct scores and bids for different players" in:
        val sb = Scoreboard.empty
          .addScore(p1, r1, Score(50), b1)
          .addScore(p2, r1, Score(-10), b2)

        sb.getStatsForRound(r1, p1)._1.value shouldBe 50
        sb.getStatsForRound(r1, p2)._1.value shouldBe -10

    "handling negative values" should:
      "allow negative scores to be recorded for a round" in:
        val sb = Scoreboard.empty.addScore(p1, r1, Score(-15), b1)

        val (score, _) = sb.getStatsForRound(r1, p1)
        score.value shouldBe -15
