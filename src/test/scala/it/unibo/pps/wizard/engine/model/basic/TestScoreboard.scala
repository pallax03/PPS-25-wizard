package it.unibo.pps.wizard.engine.model.basic

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestScoreboard extends AnyWordSpec with Matchers:
  import Scoreboard.*

  val p1: PlayerId = PlayerId(1)
  val p2: PlayerId = PlayerId(2)

  "A Scoreboard" when:
    "empty" should:
      val sb = Scoreboard.empty
      "return 0 points for any player" in:
        sb(p1) shouldBe 0
        sb(p2) shouldBe 0

    "updated" should:
      "correctly store points for a player" in:
        val sb = Scoreboard.empty.updateScore(p1, 20)
        sb(p1) shouldBe 20

      "accumulate points correctly over multiple updates" in:
        val sb = Scoreboard.empty
          .updateScore(p1, 20)
          .updateScore(p1, 10)
        sb(p1) shouldBe 30

      "maintain distinct scores for different players" in:
        val sb = Scoreboard.empty
          .updateScore(p1, 50)
          .updateScore(p2, -10)
        sb(p1) shouldBe 50
        sb(p2) shouldBe -10

    "handling negative values" should:
      "allow deduction of points" in:
        val sb = Scoreboard.empty.updateScore(p1, 30).updateScore(p1, -40)
        sb(p1) shouldBe -10