package it.unibo.pps.wizard.application.scalafx.controllers.gameboard

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.util.{PresentationQueue, PresentationStep}
import it.unibo.pps.wizard.engine.events.{
  ActionEvent,
  InvitationEvent,
  LifecycleEvent,
  ProgressEvent,
  WizardEvent
}

import scala.concurrent.ExecutionContext.Implicits.global

class GameBoardEventDispatcher(private val view: GameBoardView)(using
    context: WizardApplicationContext
):
  private val presentation: PresentationQueue = PresentationQueue()
  private var subscriptionIds: List[String] = Nil
  
  def startListening(): Unit =
    context.inboundPort
      .subscribe[WizardEvent](event => presentation.enqueue(toPresentationStep(event)))
      .foreach(id => subscriptionIds = id :: subscriptionIds)

  def stopListening(): Unit =
    context.inboundPort.unsubscribe(subscriptionIds*)
    subscriptionIds = Nil

  private def toPresentationStep(event: WizardEvent): PresentationStep =
    event match
      case ActionEvent.CardPlayed(playerId, card, _, _) =>
        PresentationStep.after(200):
          view.displayCardPlayed(playerId, card)

      case ActionEvent.TrumpColorResolved(playerId, color) =>
        PresentationStep.immediate:
          view.displayTrumpSelected(playerId, color)

      case ActionEvent.BidPlaced(playerId, bid) =>
        PresentationStep.after(200):
          view.displayBidPlaced(playerId, bid)

      case ProgressEvent.CardsDealt(playerId, hands, trump, round) =>
        PresentationStep.immediate:
          view.displayCardsDealt(playerId, hands, trump, round)

      case ProgressEvent.TrickWon(winnerId, tricksWon, trickedCards) =>
        PresentationStep.before(2000):
          view.displayTrickWon(winnerId, tricksWon, trickedCards)

      case ProgressEvent.RoundScored(scoreboard, players) =>
        PresentationStep.immediate:
          view.displayRoundScored(scoreboard, players)

      case ProgressEvent.PhaseChanged(phase) =>
        PresentationStep.immediate:
          view.displayPhaseChanged(phase)

      case ProgressEvent.IsTurnOf(_, _) =>
        PresentationStep.noop

      case InvitationEvent.WaitingForTrump(playerId) =>
        PresentationStep.after(200):
          view.displayTurnChanged(playerId, "ChoosingTrump")
          view.displayWaitingForTrump(playerId)

      case InvitationEvent.WaitingForBid(playerId, _) =>
        PresentationStep.after(200):
          view.displayTurnChanged(playerId, "Bidding")

      case InvitationEvent.WaitingForCard(playerId, legalCards) =>
        PresentationStep.after(200):
          view.displayTurnChanged(playerId, "Playing")
          view.displayLegalCards(playerId, legalCards)

      case LifecycleEvent.GameStarted(players, _) =>
        PresentationStep.immediate:
          view.displayGameStarted(players)

      case LifecycleEvent.GameEnded(scoreboard, players) =>
        PresentationStep.immediate:
          view.displayGameEnded(scoreboard, players)
