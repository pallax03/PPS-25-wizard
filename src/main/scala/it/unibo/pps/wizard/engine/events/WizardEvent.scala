package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.Card
import it.unibo.pps.wizard.engine.model.core.GameState

sealed trait WizardEvent extends Event

object WizardEvent:
  case class GameStarted(initialState: GameState) extends WizardEvent

  case class CardPlayed(playerId: String, card: Card) extends WizardEvent
  case class BidPlaced(playerId: String, bid: Int) extends WizardEvent

  case class TurnChanged(activePlayerId: String) extends WizardEvent
  case class RoundEnded(scores: Map[String, Int]) extends WizardEvent

  case class ActionFailed(playerId: String, reason: String) extends WizardEvent