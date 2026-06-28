package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.*

sealed trait WizardEvent extends Event

object WizardEvent:
  case class GameStarted(initialState: GameState) extends WizardEvent
  case class GameEnded(finalScores: Scoreboard) extends WizardEvent

  private case class CardPlayed(playerId: PlayerId, card: Card) extends WizardEvent
  private case class TrumpSelected(playerId: PlayerId, color: Card.Color) extends WizardEvent
  private case class BidPlaced(playerId: PlayerId, bid: Bid) extends WizardEvent
  private case class TrickWon(winnerId: PlayerId, trickedCards: List[Card]) extends WizardEvent

  case class RoundScored(scoreboard: Scoreboard) extends WizardEvent
  case class CardsDealt(hands: Hands, trump: Trump) extends WizardEvent
  case class PhaseChanged(state: GameState) extends WizardEvent

  case class ActionFailed(playerId: PlayerId, reason: String) extends WizardEvent

  def mapActionToEvent(action: GameAction): WizardEvent =
    action match
      case GameAction.ChooseTrump(playerId, color) => TrumpSelected(playerId, color)
      case GameAction.PlaceBid(playerId, bid) => BidPlaced(playerId, bid)
      case GameAction.PlayCard(playerId, card) => CardPlayed(playerId, card)

  def composeEvents(baseEvent: WizardEvent, oldState: GameState, newState: GameState): List[WizardEvent] =
    val composedEvents: List[WizardEvent] = (oldState, newState) match
      case (GameState.Playing(_, _, _, oldTable, _, _), GameState.Playing(_, _, _, newTable, winnerId, _))
        if oldTable.playedCards.nonEmpty && newTable.playedCards.isEmpty => List(TrickWon(winnerId, oldTable.playedCards))
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
      case (oldState, newState) if oldState.getClass != newState.getClass => List(PhaseChanged(newState))
      case _ => List.empty
    baseEvent +: composedEvents