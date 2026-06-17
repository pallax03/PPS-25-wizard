package it.unibo.pps.wizard.model.basic

import it.unibo.pps.wizard.model.basic.{Card, Hand, Player}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestPlayer extends AnyWordSpec with Matchers:
  import Card.Color.*
  import Card.Rank.*

  "A Player" should:
    val initialValue = 0
    val p = Player.human(initialValue, "Alice")
    "have the correct name and id" in:
      p.name shouldBe "Alice"
      p.id shouldBe initialValue
    "start with an empty hand, no bid, zero tricks won and zero points" in:
      p.hand.size shouldBe initialValue
      p.bid shouldBe None
      p.tricksWon shouldBe initialValue
      p.points shouldBe initialValue
    "receive cards and update hand correctly" in:
      val cardsToReceive = List(Card(Red, One), Card(Blue, Two), Card(Green, Three))
      val updatedPlayer = p.receiveCards(cardsToReceive)
      updatedPlayer.hand.size shouldBe cardsToReceive.size
      updatedPlayer.hand.cards shouldBe cardsToReceive.toSet
    "play a card successfully and update hand" in:
      val playerWithCards = p.receiveCards(List(Card(Red, One), Card(Blue, Two)))
      val cardToPlay = Card(Red, One)
      val playResult = playerWithCards.playCard(cardToPlay)
      playResult shouldBe Right((playerWithCards.copy(hand = Hand(Set(Card(Blue, Two))), bid = None, tricksWon = 0, points = 0), cardToPlay))
    "attempt to play a card not in hand and receive an error" in:
      val playerWithCards = p.receiveCards(List(Card(Red, One), Card(Blue, Two)))
      val cardNotInHand = Card(Green, Three)
      val playResult = playerWithCards.playCard(cardNotInHand)
      playResult shouldBe Left(s"Card $cardNotInHand not in player's hand")