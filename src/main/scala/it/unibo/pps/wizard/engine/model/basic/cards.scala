package it.unibo.pps.wizard.engine.model.basic

/**
 * A card in the Wizard game.
 *
 * Implemented as a sealed trait with three concrete kinds:
 *  - Standard: a color + rank card (1..13)
 *  - Wizard: the special Wizard card
 *  - Jester: the special Jester card
 *
 * Companion object provides enums for Color and Rank
 * and factory methods.
 */
sealed trait Card
sealed trait SpecialCard extends Card:
  def id: Int

object Card:
  enum Color:
    case Blue, Green, Red, Yellow
  /**
   * Rank in the standard color cards. Values 1..13 (1 is low, 13 is high).
   */
  enum Rank(val value: Int):
    case One    extends Rank(1)
    case Two    extends Rank(2)
    case Three  extends Rank(3)
    case Four   extends Rank(4)
    case Five   extends Rank(5)
    case Six    extends Rank(6)
    case Seven  extends Rank(7)
    case Eight  extends Rank(8)
    case Nine   extends Rank(9)
    case Ten    extends Rank(10)
    case Eleven extends Rank(11)
    case Twelve extends Rank(12)
    case Thirteen extends Rank(13)

  final case class Standard(color: Color, rank: Rank) extends Card
  final case class Wizard(id: Int) extends SpecialCard
  final case class Jester(id: Int) extends SpecialCard

  def apply(color: Color, rank: Rank): Card = Standard(color, rank)
  def wizard(id: Int): Card = Wizard(id)
  def jester(id: Int): Card = Jester(id)

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
  import cats.data.State
  import cats.implicits.*

  private final val TOTAL_WIZARD: Int = 4
  private final val TOTAL_JESTER: Int = 4
  final val TOTAL_SIZE: Int = TOTAL_JESTER + TOTAL_WIZARD + (Card.Rank.values.length * Card.Color.values.length)

  def create(cards: Card*): Deck = cards.toList.distinct
  def create: Deck = DeckFactory.create()

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
    import scala.util.Random
    def create(): Deck =
      val standards = (Card.Color.values.toList, Card.Rank.values.toList).mapN(Card(_, _))

      val wizards = (0 until TOTAL_WIZARD).map(Card.Wizard(_)).toList
      val jesters = (0 until TOTAL_JESTER).map(Card.Jester(_)).toList

      Random.shuffle(standards |+| wizards |+| jesters)

opaque type Hand = List[Card]
object Hand:
  def empty: Hand = List.empty
  def apply(cards: Card*): Hand = cards.toList
  def fromList(cards: List[Card]): Hand = cards

  extension (h: Hand)
    def isEmpty: Boolean = h.isEmpty
    def contains(card: Card): Boolean = h.contains(card)
    def receive(newCards: List[Card]): Hand = h ++ newCards
    def play(card: Card): Hand = h.filterNot(_ == card)
    def toList: List[Card] = h

opaque type Hands = Map[PlayerId, Hand]
object Hands:
  def empty: Hands = Map.empty

  extension (hands: Hands)
    def getHand(player: PlayerId): Option[Hand] = hands.get(player)