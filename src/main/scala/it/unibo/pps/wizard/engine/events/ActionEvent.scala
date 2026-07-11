package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.*

sealed trait ActionEvent extends WizardEvent

object ActionEvent:
  case class TrumpColorResolved(playerId: PlayerId, color: Card.Color) extends ActionEvent
  case class CardPlayed(playerId: PlayerId, card: Card) extends ActionEvent
  case class BidPlaced(playerId: PlayerId, bid: Bid) extends ActionEvent
