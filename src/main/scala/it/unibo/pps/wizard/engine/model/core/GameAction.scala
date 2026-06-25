package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.rules.*

sealed trait GameAction
object GameAction:
  case class PlaceBid(playerId: PlayerId, bid: Bid) extends GameAction
  case class ChooseTrump(playerId: PlayerId, color: Card.Color) extends GameAction
  case class PlayCard(playerId: PlayerId, card: Card) extends GameAction