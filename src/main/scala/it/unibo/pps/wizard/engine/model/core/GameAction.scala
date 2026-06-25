package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.*

sealed trait GameAction:
  def playerId: PlayerId
object GameAction:
  case class PlaceBid(playerId: PlayerId, bid: Bid) extends GameAction
  case class ChooseTrump(playerId: PlayerId, color: Card.Color) extends GameAction
  case class PlayCard(playerId: PlayerId, card: Card) extends GameAction