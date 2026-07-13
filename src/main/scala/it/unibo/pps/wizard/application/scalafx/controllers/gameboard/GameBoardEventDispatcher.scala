package it.unibo.pps.wizard.application.scalafx.controllers.gameboard

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.util.{PresentationQueue, PresentationStep}
import it.unibo.pps.wizard.engine.events.{ActionEvent, InvitationEvent, LifecycleEvent, ProgressEvent, WizardEvent}

class GameBoardEventDispatcher(private val view: GameBoardView)(using
    context: WizardApplicationContext
):
  private val presentation: PresentationQueue = PresentationQueue()

  def startListening(): Unit =
    context.inboundPort.subscribe[WizardEvent](event => presentation.enqueue(toPresentationStep(event)))

  private def toPresentationStep(event: WizardEvent): PresentationStep =
    event match
      case ActionEvent.CardPlayed(playerId, card) =>
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

      case ProgressEvent.TrickWon(winnerId, trickedCards) =>
        PresentationStep.before(2000):
          view.displayTrickWon(winnerId, trickedCards)

      case ProgressEvent.RoundScored(scoreboard) =>
        PresentationStep.immediate:
          view.displayRoundScored(scoreboard)

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

      case InvitationEvent.WaitingForCard(playerId, _) =>
        PresentationStep.after(200):
          view.displayTurnChanged(playerId, "Playing")

      case LifecycleEvent.GameStarted(players, _) =>
        PresentationStep.immediate:
          view.displayGameStarted(players)
