package it.unibo.pps.wizard.engine.model.basic

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.postfixOps

class TestTable extends AnyWordSpec with Matchers:
  import Card.*
  import Table.*
  "A Table" when:
    val p1 = PlayerId(1)
    val p2 = PlayerId(2)
    val p3 = PlayerId(3)
    "empty" should:
      val table = Table.empty
      "be empty and have no leader" in:
        table.isEmpty shouldBe true
        table.leaderCard shouldBe None

    "receiving plays" should:
      val cardP1: Card = 10.red
      val cardP2: Card = wizard
      val table = Table.empty
        + (p1 plays cardP1)
        + (p2 plays cardP2)
      "store the plays in chronological order" in:
        table.size shouldBe 2
        table.playedCards shouldEqual (cardP1 - cardP2)
        table.plays shouldEqual List((p1, cardP1), (p2, cardP2))

      "identify the player of a specific card" in:
        table.playerOf(cardP1) shouldBe Some(p1)
        table.playerOf(cardP2) shouldBe Some(p2)
        table.playerOf(5.blue) shouldBe None

    "evaluating the leader card (suit to follow)" should:
      "set the first standard card as leader" in:
        val t = Table.empty + (p1 plays 4.blue) + (p2 plays 10.red)
        t.leaderCard shouldBe Some(4 of Blue)

      "ignore leading Jesters and take the next standard card" in:
        val t = Table.empty + (p1 plays jester) + (p2 plays 8.green) + (p3 plays 2.green)
        t.leaderCard shouldBe Some(8 of Green)

      "have NO leader if the first non-Jester card is a Wizard" in:
        val t = Table.empty + (p1 plays jester) + (p2 plays wizard) + (p3 plays 10.yellow)
        t.leaderCard shouldBe None

      "have NO leader if only Jesters are played" in:
        val t = Table.empty + (p1 plays jester) + (p2 plays jester)
        t.leaderCard shouldBe None