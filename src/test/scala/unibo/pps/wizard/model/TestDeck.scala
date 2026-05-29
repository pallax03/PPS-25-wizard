package unibo.pps.wizard.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import unibo.pps.wizard.model.Card.Color.*
import unibo.pps.wizard.model.Card.Rank.*


class TestDeck extends AnyWordSpec with Matchers:
  import Deck.*
  "A new randomized Deck" should:
    val d = Deck()
    "have a size of 60 cards" in:
      d.length shouldBe TOTAL_SIZE

//    "be randomized" in:
//      ???

  "A custom Deck" should:
    val d = Deck(Card(Red, One), Card(Blue, One), Card(Green, Thirteen), Card(Blue, One))
    "have a size of 3 cards, checking for duplicates" in:
      d.length shouldBe 3
    "pop 3 cards in the same order" in:
      val drawnAction = Deck.pop(d.length)
      val (remainingDeck, drawnCards) = drawnAction.run(d).value
      drawnCards.length shouldBe 3
      remainingDeck.length shouldBe 0
      drawnCards shouldEqual List(
        Card(Red, One),
        Card(Blue, One),
        Card(Green, Thirteen)
      )
//    "pop 4 cards (out of cards), should Throw an Exception" in:
//      val drawnAction = Deck.pop(d.length+1)
//      val (remainingDeck, drawnCards) = drawnAction.run(d).value
//      ???

