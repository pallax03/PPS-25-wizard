package it.unibo.pps.wizard.engine.model.basic.cards

import it.unibo.pps.wizard.engine.model.basic.Card.{jester, wizard}
import it.unibo.pps.wizard.engine.model.basic.{Card, Hand, Hands, PlayerId}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.postfixOps

class TestHands extends AnyWordSpec with Matchers:
  import Card.*
  import Hand.*
  "A Hand" when:
    "created empty" should:
      val emptyHand = Hand.empty
      "have size 0 and be completely empty" in:
        emptyHand.isEmpty shouldBe true
        emptyHand.size shouldBe 0
        emptyHand.toList shouldBe empty

    "interacting with cards" should:
      val c1 = 5.blue
      val c2 = jester
      val notInHand = wizard
      val hand = Hand(c1 - c2)
      "add new card" in:
        hand.contains(notInHand) shouldBe false
        val updated = hand + notInHand
        updated.size shouldBe 2
        updated.contains(c1) shouldBe true
        updated.contains(notInHand) shouldBe true

      "play (remove) a card correctly" in:
        val updated = hand - c1
        updated.size shouldBe 1
        updated.contains(c1) shouldBe false
        updated.contains(c2) shouldBe true

      "do nothing when playing a card not inside hand" in:
        val updated = hand - notInHand
        updated.size shouldBe hand.size
        updated.toList shouldEqual hand.toList

  "A Hands" should:
    val p1 = PlayerId(1)
    val p2 = PlayerId(2)
    var hands = Hands.empty
    "manage multiple player hands elegantly" in:
      hands = hands + (p1 -> Hand(1.red - 2.blue))
      hands = hands + (p2 -> Hand(wizard - jester))

      hands.getHand(p1).get.size shouldBe 2
      hands.getHand(p2).get.contains(wizard) shouldBe true
      hands.getHand(PlayerId(3)) shouldBe None