package it.unibo.pps.wizard.engine.model.basic

object Table:
  opaque type Table = List[(PlayerId, Card)]
  def empty: Table = List.empty

  extension (t: Table)
    def addCard(player: PlayerId, card: Card): Table = t :+ (player, card)
    def playedCards: List[Card] = t.map((_, card) => card)
    def leaderColor: Option[Card.Color] = t.playedCards.collectFirst:
        case Card.Standard (color, _) => color
    def playerOf(card: Card): Option[PlayerId] = t.find((_, c) => c == card).map((player, _) => player)

export Table.*