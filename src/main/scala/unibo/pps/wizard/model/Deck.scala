package unibo.pps.wizard.model


/**
 *  Functional Object to create a shuffled Deck for Wizard, Composed of:
 *  - 13 Cards for every Color (4), Ranked from 1 to 13. -> 52 Ranked Cards
 *  - 4 Wizard card
 *  - 4 Jester card
 *  Total Number of Cards = 60.
 *  Each Round a new Deck need to be initialized.
 *  This deck need to contain 60 unique cards, and every player cannot receive a duplicate.
 */
object Deck:
//  final val Int TOTAL_SIZE = 60

  opaque type Deck = Set[Card]

  /**
   * @param n -> number of cards you want to receive from main deck.
   * @return
   */
  def pop(n: Int = 1): Deck = ???

  def lenght: Deck = ???


/**
 * From an FP point of view analyzing pop function,
 * how can we return the cards,
 * and at the same time, return the remaining Deck?
 * Maybe we need a more OOP approach?
*/