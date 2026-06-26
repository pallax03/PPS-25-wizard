package it.unibo.pps.wizard.engine.model.basic

import it.unibo.pps.wizard.engine.model.basic.Card.{Color, Rank}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TestTable extends AnyWordSpec with Matchers:
  "A Table" when:
    val t: Table = Table.empty
    val p1: PlayerId = PlayerId(1)
    val p2: PlayerId = PlayerId(2)
    "empty" should:
      "have no played cards" in:
        t.playedCards shouldBe empty
      "have no leader color" in:
        t.leaderCard shouldBe empty
    "adding cards" should:
      val card1 = Card.wizard(1)
      val card2 = Card(Color.Red, Rank.Ten)
      "contain the card" in:
        val newTable = t.addCard(p1, card1)
        newTable.playedCards should contain only card1
        newTable.playerOf(card1).get shouldBe p1
      "preserve the exact order of play" in:
        val newTable = t
          .addCard(p1, card1)
          .addCard(p2, card2)
        newTable.playedCards shouldEqual List(card1, card2)
        newTable.playerOf(card1).get shouldBe p1
        newTable.playerOf(card2).get shouldBe p2