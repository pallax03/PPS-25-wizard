package it.unibo.pps.wizard.model.basic

import it.unibo.pps.wizard.model.basic.{Card, PlayerId}

object Table:

  opaque type Table = List[(PlayerId, Card)]
  def empty: Table = List.empty

  extension (t: Table)
    def addCard(player: PlayerId, card: Card): Table = t :+ (player, card)
    def playedCards: List[Card] = t.map((_, card) => card)
    def leaderColor: Option[Card.Color] = t.playedCards.collectFirst:
        case Card.Standard (color, _) => color
        
export Table.*