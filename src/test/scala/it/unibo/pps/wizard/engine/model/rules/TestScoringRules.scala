package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TestScoringRules extends AnyWordSpec with Matchers:

  import ScoringRules.*

  val p1: PlayerId = PlayerId(1)
  val p2: PlayerId = PlayerId(2)
  val p3: PlayerId = PlayerId(3)
  val players: List[Player] = List(Player.human(p1), Player.human(p2), Player.human(p3))

  "ScoringRules calculation" when:
    "a player matches their bid" should:
      "award 20 points for 0 tricks" in:
        Bid(0).scoreAgainst(0) shouldBe 20

      "award base 20 + 10 per trick for exact positive bid" in:
        Bid(2).scoreAgainst(2) shouldBe 40 // 20 + 2*10

    "a player fails their bid" should:
      "deduct 10 points per trick of difference when winning more" in:
        Bid(1).scoreAgainst(3) shouldBe -20 // diff 2 * 10 = 20 penalty

      "deduct 10 points per trick of difference when winning fewer" in:
        Bid(3).scoreAgainst(0) shouldBe -30 // diff 3 * 10 = 30 penalty

  "ScoringRules integration" should:
    "correctly update the scoreboard for all players" in:
      val bids = Bids.empty + (p1 -> Bid(1)) + (p2 -> Bid(2))
      val tricks = Tricks(Map(p1 -> 1, p2 -> 2))
      val initialScoreboard = Scoreboard.empty.updateScore(p1, 50)

      val finalScoreboard = compute(players, bids, tricks, initialScoreboard)

      finalScoreboard(p1) shouldBe 80 // 50 + 30 (20+10)
      finalScoreboard(p2) shouldBe 40 // 0 + 40 (20+20)
