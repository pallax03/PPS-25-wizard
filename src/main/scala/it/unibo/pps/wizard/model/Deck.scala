package it.unibo.pps.wizard.model

import cats.data.State
import cats.implicits.{catsSyntaxSemigroup, catsSyntaxTuple2Semigroupal}

import scala.util.Random

/**
 *  Functional Object to create a shuffled Deck for Wizard, Composed of:
 *  - 13 Cards for every Color (4), Ranked from 1 to 13. -> 52 Ranked Cards
 *  - 4 Wizard card
 *  - 4 Jester card
 *  Total Number of Cards = 60.
 *  Each Round a new Deck need to be initialized.
 *  This deck need to contain 60 unique cards, and every player cannot receive a duplicate.
 */
opaque type Deck = List[Card]

object Deck:
  private final val TOTAL_WIZARD: Int = 4
  private final val TOTAL_JESTER: Int = 4
  final val TOTAL_SIZE: Int = TOTAL_JESTER + TOTAL_WIZARD + (Card.Rank.values.length * Card.Color.values.length)

  def apply(cards: Card*): Deck = cards.toList.distinct
  def apply(): Deck = DeckFactory.create()

  extension (d: Deck)
    def length: Int = d.length

  /**
   * @param n -> number of cards you want to receive from main deck.
   *  * From an FP point of view analyzing pop function,
   *  * how can we return the cards,
   *  * and at the same time, return the remaining Deck?
   *  * Using Cats.State
   *
   * @return  State[Deck, drawnCards]
   */
  def pop(n: Int): State[Deck, List[Card]] =
    State: (currentDeck: Deck) =>
      require(currentDeck.length >= n)
      currentDeck.splitAt(n).swap

  private object DeckFactory:
    def create(): Deck =
      val standards = (Card.Color.values.toList, Card.Rank.values.toList).mapN(Card(_, _))

      val wizards = (0 until TOTAL_WIZARD).map(Card.Wizard(_)).toList
      val jesters = (0 until TOTAL_JESTER).map(Card.Jester(_)).toList

      Random.shuffle(standards |+| wizards |+| jesters)