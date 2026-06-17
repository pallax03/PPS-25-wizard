package it.unibo.pps.wizard.engine.model.basic

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TestTable extends AnyWordSpec with Matchers:
  "A Table" when:
    val t: Table = Table.empty
    "empty" should:
      "have no played cards" in:
        t.playedCards shouldBe empty
      "have no leader color" in:
        t.leaderColor shouldBe empty
    "adding cards" should:
      val card1 = Card.wizard(1)
      val card2 = Card(Color.Red, Rank.Ten)
      "contain the card" in:
        val newTable = t.addCard(PlayerId(1), card1)
        newTable.playedCards should contain only card1
      "preserve the exact order of play" in:
        val newTable = t
          .addCard(PlayerId(1), card1)
          .addCard(PlayerId(2), card2)
        newTable.playedCards shouldEqual List(card1, card2)