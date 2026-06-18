package it.unibo.pps.wizard.engine.model.basic

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestHand extends AnyWordSpec with Matchers:

  "A Hand" when:
    val emptyHand = Hand.empty
    "empty" should:
      "have size 0 and be completely empty" in:
        emptyHand.size shouldBe 0
        emptyHand.isEmpty shouldBe true
        emptyHand.toList shouldBe empty
    "with 2 cards" should:
      val newCards = List(Card.jester(2), Card.Standard(Color.Blue, Rank.Five))
      val notCard = Card.wizard(2)
      val filledHand = emptyHand.receive(newCards)
      "add new cards" in:
        filledHand.size shouldBe newCards.length
        emptyHand.size shouldBe 0
        emptyHand.contains(notCard) shouldBe false
      "playing a card" in:
        val card = newCards.head
        val updated = filledHand.play(card)
        updated.size shouldBe (newCards.length - 1)
        updated.toList shouldEqual newCards.filterNot(_ == card)
      "playing a card not inside hand" in:
        val updated = filledHand.play(notCard)
        updated.size shouldBe filledHand.size
        updated.toList shouldEqual filledHand.toList
        