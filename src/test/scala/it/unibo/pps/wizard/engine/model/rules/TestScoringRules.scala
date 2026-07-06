package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TestScoringRules extends AnyWordSpec with Matchers:

  import ScoringRules.*

  val p0: Player = Player.human(PlayerId(0), PlayerName("Alice"))
  val p1: Player = Player.human(PlayerId(1), PlayerName("Bob"))
  val p2: Player = Player.human(PlayerId(2), PlayerName("Charlie"))
  val players: Players = Players(p0, p1, p2)

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
      val bids = Bids.empty + (p1.id -> Bid(1)) + (p2.id -> Bid(2))
      val tricks = Tricks(Map(p1.id -> 1, p2.id -> 2))
      val initialScoreboard = Scoreboard.empty.updateScore(p1.id, 50)

      val finalScoreboard = compute(players, bids, tricks, initialScoreboard)

      finalScoreboard(p1.id) shouldBe 80 // 50 + 30 (20+10)
      finalScoreboard(p2.id) shouldBe 40 // 0 + 40 (20+20)
