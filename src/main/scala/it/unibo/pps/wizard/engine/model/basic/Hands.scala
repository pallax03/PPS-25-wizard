package it.unibo.pps.wizard.engine.model.basic

opaque type Hands = Map[PlayerId, Hand]

object Hands:
  def empty: Hands = Map.empty
  
  extension (hands: Hands)
    def getHand(player: PlayerId): Option[Hand] = hands.get(player)