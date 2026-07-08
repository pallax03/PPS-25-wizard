package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameAction

sealed trait ActionEvent extends WizardEvent

object ActionEvent:
  case class CardPlayed(playerId: PlayerId, card: Card) extends ActionEvent
  case class TrumpColorResolved(playerId: PlayerId, color: Card.Color) extends ActionEvent
  case class BidPlaced(playerId: PlayerId, bid: Bid) extends ActionEvent

  def from(action: GameAction): ActionEvent = action match
    case GameAction.ResolveTrumpColor(playerId, color) => TrumpColorResolved(playerId, color)
    case GameAction.PlaceBid(playerId, bid)      => BidPlaced(playerId, bid)
    case GameAction.PlayCard(playerId, card)     => CardPlayed(playerId, card)
