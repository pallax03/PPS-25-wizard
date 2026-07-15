package it.unibo.pps.wizard.application.scalafx.controllers.gameboard

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.util.{PresentationQueue, PresentationScript, PresentationStep, UiPhase}
import it.unibo.pps.wizard.engine.events.{ActionEvent, FailureEvent, InvitationEvent, LifecycleEvent, ProgressEvent, WizardEvent}
import it.unibo.pps.wizard.engine.model.core.{CardNotAllowedReasons, GameError}

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
      case ActionEvent.CardPlayed(playerId, playerName, card, winningCard, followingColor) =>
        PresentationScript(
          run(view.displayCardPlayed(playerId, playerName, card, winningCard, followingColor)),
          waitFor(350)
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
            view.displayTurnChanged(playerId, UiPhase.ChoosingTrump)
            view.displayWaitingForTrump(playerId),
          waitFor(200)
        )

      case InvitationEvent.WaitingForBid(playerId, _) =>
        PresentationScript(
          run(view.displayTurnChanged(playerId, UiPhase.Bidding)),
          waitFor(200)
        )

      case InvitationEvent.WaitingForCard(playerId, legalCards) =>
        PresentationScript(
          run:
            view.displayTurnChanged(playerId, UiPhase.Playing)
            view.displayLegalCards(playerId, legalCards),
          waitFor(200)
        )

      case LifecycleEvent.GameStarted(players, _) =>
        PresentationScript(run(view.displayGameStarted(players)))

      case LifecycleEvent.GameEnded(scoreboard, players) =>
        PresentationScript(run(view.displayGameEnded(scoreboard, players)))

      case FailureEvent.ActionFailed(playerId, error) => error match
        case GameError.NotYourTurn =>
          PresentationScript(run(view.displayErrorMessage("It's not your turn."))) 
        case GameError.CardNotAllowed(reason) => reason match
          case CardNotAllowedReasons.MustFollowColor(requiredColor, cards) =>
            PresentationScript(run(view.displayErrorMessage(s"Must follow color $requiredColor.")))
          case _ => PresentationScript()
        case GameError.InvalidBid if playerId == view.getCurrentPlayerId =>
          PresentationScript(
            run:
              view.displayErrorMessage("Invalid bid.")
              view.displayShowInvalidBid(),
            waitFor(3000),
            run(view.displayClearInvalidBid())
          )
        case error: GameError => PresentationScript(run(view.displayErrorMessage(error.toString)))

  private def run(action: => Unit): PresentationStep = PresentationStep.run(action)

  private def waitFor(delayMs: Double): PresentationStep = PresentationStep.waitFor(delayMs)
