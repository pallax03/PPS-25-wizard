package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.events.LifecycleEvent.GameEnded
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameState

sealed trait ProgressEvent extends WizardEvent

object ProgressEvent:
  case class CardsDealt(hands: Hands, trump: Trump) extends ProgressEvent
  case class TrickWon(winnerId: PlayerId, trickedCards: List[Card]) extends ProgressEvent
  case class RoundScored(scoreboard: Scoreboard) extends ProgressEvent
  case class PhaseChanged(state: GameState) extends ProgressEvent

  def fromTransition(oldState: GameState, newState: GameState): List[WizardEvent] =
    (oldState, newState) match
      case (
            GameState.Playing(_, _, _, oldTable, _, _),
            GameState.Playing(_, _, _, newTable, winnerId, _)
          ) if oldTable.playedCards.nonEmpty && newTable.playedCards.isEmpty =>
        List(TrickWon(winnerId, oldTable.playedCards))
      case (oldS: GameState.Playing, newS: GameState.Bidding) =>
        List(
          TrickWon(newS.currentPlayer, oldS.table.playedCards),
          RoundScored(newS.core.scoreboard),
          CardsDealt(newS.core.hands, newS.trump),
          PhaseChanged(newS)
        )
      case (oldS: GameState.Playing, newS: GameState.Ended) =>
        List(
          TrickWon(oldS.currentPlayerTurn, oldS.table.playedCards),
          RoundScored(newS.scoreboard),
          GameEnded(newS.scoreboard)
        )
      case (oldState, newState) if oldState.getClass != newState.getClass =>
        List(PhaseChanged(newState))
      case _ =>
        List.empty
