package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.*

sealed trait ProgressEvent extends WizardEvent

object ProgressEvent:
  case class CardsDealt(playerId: PlayerId, hands: Hands, trump: Trump, round: Round)
      extends ProgressEvent
  case class TrickWon(winnerId: PlayerId, tricksWon: Int, trickedCards: List[Card]) extends ProgressEvent
  case class IsTurnOf(currentPlayer: PlayerId, phase: String) extends ProgressEvent
  case class RoundScored(scoreboard: Scoreboard) extends ProgressEvent
  case class PhaseChanged(phaseName: String) extends ProgressEvent
