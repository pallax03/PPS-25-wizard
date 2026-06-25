package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameState

sealed trait WizardEvent extends Event

object WizardEvent:
  case class GameStarted(initialState: GameState) extends WizardEvent

  case class CardPlayed(playerId: PlayerId, card: Card) extends WizardEvent
  case class BidPlaced(playerId: PlayerId, bid: Bid) extends WizardEvent

  case class TurnChanged(activePlayerId: PlayerId) extends WizardEvent
  case class RoundEnded(scores: Scoreboard) extends WizardEvent

  case class ActionFailed(playerId: PlayerId, reason: String) extends WizardEvent