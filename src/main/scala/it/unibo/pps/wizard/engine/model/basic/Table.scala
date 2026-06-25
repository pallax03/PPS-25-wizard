package it.unibo.pps.wizard.engine.model.basic

opaque type Table = List[(PlayerId, Card)]

object Table:
  def empty: Table = List.empty

  extension (t: Table)
    def addCard(player: PlayerId, card: Card): Table = t :+ (player, card)
    def playedCards: List[Card] = t.map((_, card) => card)
    def leaderCard: Option[Card.Standard] = t.playedCards.collectFirst:
      case s: Card.Standard => s
    def playerOf(card: Card): Option[PlayerId] = t.find((_, c) => c == card).map((player, _) => player)
