package it.unibo.pps.wizard.engine.model.basic

opaque type Hand = List[Card]
opaque type Hands = Map[PlayerId, Hand]
object Hand:
  def empty: Hand = List.empty
  def apply(cards: Card*): Hand = cards.toList
  def fromList(cards: List[Card]): Hand = cards

  extension (h: Hand)
    def size: Int = h.length
    def isEmpty: Boolean = h.isEmpty
    def contains(card: Card): Boolean = h.contains(card)
    def receive(newCards: List[Card]): Hand = h ++ newCards
    def play(card: Card): Hand = h.filterNot(_ == card)
    def toList: List[Card] = h