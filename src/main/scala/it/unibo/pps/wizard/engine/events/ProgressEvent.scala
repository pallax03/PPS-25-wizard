package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.events.LifecycleEvent.GameEnded
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.{GameAction, GameState}
import it.unibo.pps.wizard.engine.model.rules.TableRules.*

sealed trait ProgressEvent extends WizardEvent

object ProgressEvent:
  case class CardsDealt(playerId: PlayerId, hands: Hands, trump: Trump, round: Round)
      extends ProgressEvent
  case class TrickWon(winnerId: PlayerId, trickedCards: List[Card]) extends ProgressEvent
  case class IsTurnOf(currentPlayer: PlayerId) extends ProgressEvent
  case class RoundScored(scoreboard: Scoreboard) extends ProgressEvent
  case class PhaseChanged(phaseName: String) extends ProgressEvent

  def fromTransition(
      oldState: GameState,
      newState: GameState,
      action: GameAction
  ): List[WizardEvent] =
    (oldState, newState, action) match
      case (
            oldS: GameState.Playing,
            newS: GameState.Playing,
            GameAction.PlayCard(playerId, card)
          ) if oldS.table.playedCards.nonEmpty && newS.table.playedCards.isEmpty =>
        List(
          trickWon(oldS, playerId, card),
          IsTurnOf(newS.currentPlayerTurn)
        )
      case (
            oldS: GameState.Playing,
            newS: GameState.Bidding,
            GameAction.PlayCard(playerId, card)
          ) =>
        List(
          trickWon(oldS, playerId, card),
          IsTurnOf(newS.currentPlayer),
          RoundScored(newS.core.scoreboard),
          CardsDealt(playerId, newS.core.hands, newS.core.trump, newS.core.round),
          PhaseChanged(newS.getClass.getSimpleName)
        )
      case (oldS: GameState.Playing, newS: GameState.Ended, GameAction.PlayCard(playerId, card)) =>
        List(
          trickWon(oldS, playerId, card),
          RoundScored(newS.scoreboard),
          GameEnded(newS.scoreboard)
        )
      case (oldState, newState, _) if oldState.getClass != newState.getClass =>
        List(PhaseChanged(newState.getClass.getSimpleName))
      case _ =>
        List.empty

  private def trickWon(state: GameState.Playing, playerId: PlayerId, card: Card): TrickWon =
    val completedTable = state.table + (playerId, card)
    val winnerId = completedTable.evaluateTrick(state.core.trump)._1
    TrickWon(winnerId, completedTable.playedCards)
