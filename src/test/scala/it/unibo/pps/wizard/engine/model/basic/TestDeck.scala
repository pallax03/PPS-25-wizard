//package it.unibo.pps.wizard.engine.model.basic
//
//import it.unibo.pps.wizard.engine.model.basic.Card.Color.*
//import it.unibo.pps.wizard.engine.model.basic.Card.Rank.*
//import it.unibo.pps.wizard.engine.model.basic.{Card, Deck}
//import org.scalatest.matchers.should.Matchers
//import org.scalatest.wordspec.AnyWordSpec
//
//
//class TestDeck extends AnyWordSpec with Matchers:
//  import Deck.*
//  "A new randomized Deck" should:
//    val d = Deck.create
//    "have a size of 60 cards" in:
//      d.length shouldBe TOTAL_SIZE
//  "A custom Deck" should:
//    val d = Deck.create(Card(Red, One), Card(Blue, One), Card(Green, Thirteen), Card(Blue, One))
//    val nCards: Int = 3
//    "have a size of 3 cards, checking for duplicates" in:
//      d.length shouldBe nCards
//    "pop 3 cards in the same order" in:
//      val drawnAction = Deck.pop(nCards)
//      val (remainingDeck, drawnCards) = drawnAction.run(d).value
//      drawnCards.length shouldBe 3
//      remainingDeck.length shouldBe 0
//      drawnCards shouldEqual List(
//        Card(Red, One),
//        Card(Blue, One),
//        Card(Green, Thirteen)
//      )
//    "pop 4 cards (out of cards), should Throw an Exception" in:
//      a [IllegalArgumentException] shouldBe thrownBy (Deck.pop(d.length+1).run(d).value)