package it.unibo.pps.wizard.engine.model.basic

opaque type Deck = List[Card]

/**
 * Represents the deck used in the Wizard Card Game.
 *
 * A standard Wizard deck is composed of 60 unique cards:
 *   - 13 Cards for every Color (4), Ranked from 1 to 13. -> 52 Ranked Cards
 *   - 4 Wizard card
 *   - 4 Jester card
 *
 * Each round requires a newly initialized deck to ensure players do not receive duplicate cards.
 */
object Deck:
  import cats.data.State

  final val TOTAL_WIZARD: Int = 4
  final val TOTAL_JESTER: Int = 4
  final val TOTAL_SIZE: Int =
    TOTAL_JESTER + TOTAL_WIZARD + (Card.Rank.values.length * Card.Color.values.length)

  /**
   * Creates a custom deck from a provided list of cards.
   *
   * @param cards the list of cards used to compose the deck. Duplicates are removed.
   * @return a custom [[Deck]].
   */
  def create(cards: List[Card]): Deck = cards.distinct

  /**
   * Creates a newly initialized and shuffled standard Wizard deck.
   *
   * @return a shuffled 60-card [[Deck]].
   */
  def create: Deck = DeckFactory.create()

  extension (d: Deck)
    def length: Int = d.length
    def cards: List[Card] = d

  /**
   * Draws a specified number of cards from the deck.
   *
   * From a Functional Programming perspective, this method uses `cats.data.State` to avoid
   * mutating the deck in place. It returns a state computation that produces both the
   * remaining deck and the drawn cards.
   *
   * @param n the number of cards to draw.
   * @note If the requested number of cards exceeds the deck's current size,
   *       all remaining cards are drawn and the new deck state becomes empty.
   *
   * @return a `State` instance representing the state transition, yielding a `List[Card]`.
   */
  def pop(n: Int): State[Deck, List[Card]] =
    State: (currentDeck: Deck) =>
      currentDeck.splitAt(n).swap

  private object DeckFactory:
    import scala.util.Random
    import Card.*
    def create(): Deck =
      val standards = for
        color <- Color.values.toList
        rank <- Rank.values.toList
      yield rank of color

      val wizards = List.fill(TOTAL_WIZARD)(wizard)
      val jesters = List.fill(TOTAL_JESTER)(jester)

      Random.shuffle(standards ++ wizards ++ jesters)

/**
 * Represents the set of cards currently held by a single player.
 * Implemented as an opaque type over List[Card] to provide domain-specific
 * operations while hiding standard collection methods.
 */
opaque type Hand = List[Card]

object Hand:
  def empty: Hand = List.empty
  def apply(cards: List[Card]): Hand = cards

  /** Removes a specific card from the hand, if present. */
  def without(hand: Hand, card: Card): Hand = hand.filterNot(_ == card)

  extension (h: Hand)
    def size: Int = h.size
    def isEmpty: Boolean = h.isEmpty
    def contains(card: Card): Boolean = h.contains(card)
    def toList: List[Card] = h

/**
 * Represents the state of all players' hands in the game.
 * Maps each PlayerId to their respective Hand.
 */
opaque type Hands = Map[PlayerId, Hand]

object Hands:
  def empty: Hands = Map.empty
  def apply(hands: Map[PlayerId, Hand]): Hands = hands

  extension (hands: Hands)
    def getHand(player: PlayerId): Option[Hand] = hands.get(player)

    /**
     * Removes a specific card from a player's hand.
     *
     * @return Some(Hands) if the player exists, None otherwise.
     */
    def remove(player: PlayerId, card: Card): Option[Hands] =
      hands.get(player).map(hand => hands.updated(player, Hand.without(hand, card)))
    def areEmpty: Boolean = hands.values.forall(_.isEmpty)
