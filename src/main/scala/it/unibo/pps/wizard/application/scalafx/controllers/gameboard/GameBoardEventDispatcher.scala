package it.unibo.pps.wizard.application.scalafx.controllers.gameboard

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.util.{PresentationQueue, PresentationScript, PresentationStep}
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
      .subscribe[WizardEvent](event => presentation.enqueue(toPresentationScript(event)))
      .foreach(id => subscriptionIds = id :: subscriptionIds)

  def stopListening(): Unit =
    context.inboundPort.unsubscribe(subscriptionIds*)
    subscriptionIds = Nil

  private def toPresentationScript(event: WizardEvent): PresentationScript =
    event match
      case ActionEvent.CardPlayed(playerId, card, _, _) =>
        PresentationScript(
          run(view.displayCardPlayed(playerId, card)),
          waitFor(200)
        )

      case ActionEvent.TrumpColorResolved(playerId, color) =>
        PresentationScript(run(view.displayTrumpSelected(playerId, color)))

      case ActionEvent.BidPlaced(playerId, bid) =>
        PresentationScript(
          run(view.displayBidPlaced(playerId, bid)),
          waitFor(200)
        )

      case ProgressEvent.CardsDealt(playerId, hands, trump, round) =>
        PresentationScript(run(view.displayCardsDealt(playerId, hands, trump, round)))

      case ProgressEvent.TrickWon(winnerId, tricksWon, trickedCards) =>
        PresentationScript(
          run(view.displayTrickWon(winnerId, tricksWon, trickedCards)),
          waitFor(3000),
          run(view.clearTable())
        )

      case ProgressEvent.RoundScored(scoreboard, players) =>
        PresentationScript(run(view.displayRoundScored(scoreboard, players)))

      case ProgressEvent.PhaseChanged(phase) =>
        PresentationScript(run(view.displayPhaseChanged(phase)))

      case ProgressEvent.IsTurnOf(_, _) =>
        PresentationScript()

      case InvitationEvent.WaitingForTrump(playerId) =>
        PresentationScript(
          run:
            view.displayTurnChanged(playerId, "ChoosingTrump")
            view.displayWaitingForTrump(playerId),
          waitFor(200)
        )

      case InvitationEvent.WaitingForBid(playerId, _) =>
        PresentationScript(
          run(view.displayTurnChanged(playerId, "Bidding")),
          waitFor(200)
        )

      case InvitationEvent.WaitingForCard(playerId, legalCards) =>
        PresentationScript(
          run:
            view.displayTurnChanged(playerId, "Playing")
            view.displayLegalCards(playerId, legalCards),
          waitFor(200)
        )

      case LifecycleEvent.GameStarted(players, _) =>
        PresentationScript(run(view.displayGameStarted(players)))

      case LifecycleEvent.GameEnded(scoreboard, players) =>
        PresentationScript(run(view.displayGameEnded(scoreboard, players)))

  private def run(action: => Unit): PresentationStep = PresentationStep.run(action)

  private def waitFor(delayMs: Double): PresentationStep = PresentationStep.waitFor(delayMs)
