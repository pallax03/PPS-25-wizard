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
      .subscribe[WizardEvent](event => presentation.enqueueAll(toPresentationSteps(event)))
      .foreach(id => subscriptionIds = id :: subscriptionIds)

  def stopListening(): Unit =
    context.inboundPort.unsubscribe(subscriptionIds*)
    subscriptionIds = Nil

  private def toPresentationSteps(event: WizardEvent): List[PresentationStep] =
    event match
      case ActionEvent.CardPlayed(playerId, card, _, _) =>
        one(PresentationStep.after(200):
          view.displayCardPlayed(playerId, card)
        )

      case ActionEvent.TrumpColorResolved(playerId, color) =>
        one(PresentationStep.immediate:
          view.displayTrumpSelected(playerId, color)
        )

      case ActionEvent.BidPlaced(playerId, bid) =>
        one(PresentationStep.after(200):
          view.displayBidPlaced(playerId, bid)
        )

      case ProgressEvent.CardsDealt(playerId, hands, trump, round) =>
        one(PresentationStep.immediate:
          view.displayCardsDealt(playerId, hands, trump, round)
        )

      case ProgressEvent.TrickWon(winnerId, tricksWon, trickedCards) =>
        List(
          PresentationStep.immediate:
            view.displayTrickWon(winnerId, tricksWon, trickedCards),
          PresentationStep.before(3000):
            view.clearTable()
        )

      case ProgressEvent.RoundScored(scoreboard, players) =>
        one(PresentationStep.immediate:
          view.displayRoundScored(scoreboard, players)
        )

      case ProgressEvent.PhaseChanged(phase) =>
        one(PresentationStep.immediate:
          view.displayPhaseChanged(phase)
        )

      case ProgressEvent.IsTurnOf(_, _) =>
        one(PresentationStep.noop)

      case InvitationEvent.WaitingForTrump(playerId) =>
        one(PresentationStep.after(200):
          view.displayTurnChanged(playerId, "ChoosingTrump")
          view.displayWaitingForTrump(playerId)
        )

      case InvitationEvent.WaitingForBid(playerId, _) =>
        one(PresentationStep.after(200):
          view.displayTurnChanged(playerId, "Bidding")
        )

      case InvitationEvent.WaitingForCard(playerId, legalCards) =>
        one(PresentationStep.after(200):
          view.displayTurnChanged(playerId, "Playing")
          view.displayLegalCards(playerId, legalCards)
        )

      case LifecycleEvent.GameStarted(players, _) =>
        one(PresentationStep.immediate:
          view.displayGameStarted(players)
        )

      case LifecycleEvent.GameEnded(scoreboard, players) =>
        one(PresentationStep.immediate:
          view.displayGameEnded(scoreboard, players)
        )

  private def one(step: PresentationStep): List[PresentationStep] = List(step)
