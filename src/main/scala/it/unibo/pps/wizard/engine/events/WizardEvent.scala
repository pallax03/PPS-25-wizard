package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.*

sealed trait WizardEvent extends Event

object WizardEvent:
  case class GameStarted(initialState: GameState) extends WizardEvent
  case class GameEnded(finalScores: Scoreboard) extends WizardEvent

  private case class CardPlayed(playerId: PlayerId, card: Card) extends WizardEvent
  private case class TrumpSelected(playerId: PlayerId, color: Color) extends WizardEvent
  private case class BidPlaced(playerId: PlayerId, bid: Bid) extends WizardEvent
  private case class TrickWon(winnerId: PlayerId, trickedCards: List[Card]) extends WizardEvent

  private case class PhaseChanged(state: GameState) extends WizardEvent

  case class ActionFailed(playerId: PlayerId, reason: String) extends WizardEvent

  private def mapActionToEvent(action: GameAction): WizardEvent =
    action match
      case GameAction.ChooseTrump(playerId, color) => TrumpSelected(playerId, color)
      case GameAction.PlaceBid(playerId, bid) => BidPlaced(playerId, bid)
      case GameAction.PlayCard(playerId, card) => CardPlayed(playerId, card)

  def generatedEvents(action: GameAction, oldState: GameState, newState: GameState): List[WizardEvent] =
    val eventOnAction: WizardEvent = mapActionToEvent(action)
    val composedEvents: List[WizardEvent] = (oldState, newState) match
      case (GameState.Playing(_, _, _, oldTable, _, _), GameState.Playing(_, _, _, newTable, winnerId, _))
        if oldTable.playedCards.nonEmpty && newTable.playedCards.isEmpty => List(TrickWon(winnerId, oldTable.playedCards))
      case (oldState, newState) if oldState.getClass != newState.getClass => List(PhaseChanged(newState))
      case _ => List.empty
    eventOnAction +: composedEvents