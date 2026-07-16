package it.unibo.pps.wizard.application.scalafx.controllers.gameboard

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.util.PresentationQueue
import it.unibo.pps.wizard.application.scalafx.util.PresentationScript
import it.unibo.pps.wizard.application.scalafx.util.PresentationStep
import it.unibo.pps.wizard.application.scalafx.util.UiPhase
import it.unibo.pps.wizard.engine.events.ActionEvent._
import it.unibo.pps.wizard.engine.events.FailureEvent._
import it.unibo.pps.wizard.engine.events.InvitationEvent._
import it.unibo.pps.wizard.engine.events.LifecycleEvent._
import it.unibo.pps.wizard.engine.events.ProgressEvent._
import it.unibo.pps.wizard.engine.events._
import it.unibo.pps.wizard.engine.model.core.CardNotAllowedReasons
import it.unibo.pps.wizard.engine.model.core.GameError

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * The GameBoardEventDispatcher class is responsible for listening to game events and dispatching them to the GameBoardView.
 * It uses a PresentationQueue to manage the presentation of events in a sequential manner.
 *
 * @param view The GameBoardView that will display the events.
 * @param context The WizardApplicationContext providing access to the inbound port for event subscription.
 */
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

  /**
   * Converts a WizardEvent into a PresentationScript that defines how the event should be presented in the UI.
   *
   * @param event The WizardEvent to be converted.
   * @return A PresentationScript representing the presentation of the event.
   */
  private def toPresentationScript(event: WizardEvent): PresentationScript =
    event match
      case e: ActionEvent     => handleActionEvent(e)
      case e: ProgressEvent   => handleProgressEvent(e)
      case e: InvitationEvent => handleInvitationEvent(e)
      case e: LifecycleEvent  => handleLifecycleEvent(e)
      case e: FailureEvent    => handleFailureEvent(e)

  /**
   * Handles ActionEvent types and creates a corresponding PresentationScript.
   *
   * @param event The ActionEvent to be handled.
   * @return A PresentationScript representing the presentation of the ActionEvent.
   */
  private def handleActionEvent(event: ActionEvent): PresentationScript =
    event match
      case CardPlayed(playerId, playerName, card, winningCard, followingColor) =>
        PresentationScript(
          run(view.displayCardPlayed(playerId, playerName, card, winningCard, followingColor)),
          waitFor(350)
        )
      case TrumpColorResolved(playerId, color) =>
        PresentationScript(run(view.displayTrumpSelected(playerId, color)))
      case BidPlaced(playerId, bid) =>
        PresentationScript(
          run(view.displayBidPlaced(playerId, bid)),
          waitFor(300)
        )

  /**
   * Handles ProgressEvent types and creates a corresponding PresentationScript.
   *
   * @param event The ProgressEvent to be handled.
   * @return A PresentationScript representing the presentation of the ProgressEvent.
   */
  private def handleProgressEvent(event: ProgressEvent): PresentationScript =
    event match
      case CardsDealt(playerId, hands, trump, round) =>
        PresentationScript(run(view.displayCardsDealt(playerId, hands, trump, round)))
      case TrickWon(winnerId, tricksWon, trickedCards) =>
        PresentationScript(
          run(view.displayTrickWon(winnerId, tricksWon, trickedCards)),
          waitFor(3000),
          run(view.clearTable())
        )
      case RoundScored(scoreboard, players) =>
        PresentationScript(run(view.displayRoundScored(scoreboard, players)))
      case PhaseChanged(phase) =>
        PresentationScript(run(view.displayPhaseChanged(phase)))

  /**
   * Handles InvitationEvent types and creates a corresponding PresentationScript.
   *
   * @param event The InvitationEvent to be handled.
   * @return A PresentationScript representing the presentation of the InvitationEvent.
   */
  private def handleInvitationEvent(event: InvitationEvent): PresentationScript =
    event match
      case WaitingForTrump(playerId) =>
        PresentationScript(
          run:
            view.displayTurnChanged(playerId, UiPhase.ChoosingTrump)
            view.displayWaitingForTrump(playerId)
          ,
          waitFor(300)
        )
      case WaitingForBid(playerId, _) =>
        PresentationScript(
          run(view.displayTurnChanged(playerId, UiPhase.Bidding)),
          waitFor(300)
        )
      case WaitingForCard(playerId, legalCards) =>
        PresentationScript(
          run:
            view.displayTurnChanged(playerId, UiPhase.Playing)
            view.displayLegalCards(playerId, legalCards)
          ,
          waitFor(300)
        )

  /**
   * Handles LifecycleEvent types and creates a corresponding PresentationScript.
   *
   * @param event The LifecycleEvent to be handled.
   * @return A PresentationScript representing the presentation of the LifecycleEvent.
   */
  private def handleLifecycleEvent(event: LifecycleEvent): PresentationScript =
    event match
      case GameStarted(players, _) =>
        PresentationScript(run(view.displayGameStarted(players)))
      case GameEnded(scoreboard, players) =>
        PresentationScript(
          run(view.displayGameEnded(scoreboard, players))
        )

  /**
   * Handles FailureEvent types and creates a corresponding PresentationScript.
   *
   * @param event The FailureEvent to be handled.
   * @return A PresentationScript representing the presentation of the FailureEvent.
   */
  private def handleFailureEvent(event: FailureEvent): PresentationScript =
    event match
      case ActionFailed(playerId, error) =>
        error match
          case GameError.NotYourTurn =>
            PresentationScript(run(view.displayErrorMessage("It's not your turn.")))
          case GameError.CardNotAllowed(reason) =>
            reason match
              case CardNotAllowedReasons.MustFollowColor(requiredColor, cards) =>
                PresentationScript(
                  run(view.displayErrorMessage(s"Must follow color $requiredColor."))
                )
              case _ => PresentationScript()
          case GameError.InvalidBid if playerId == view.getCurrentPlayerId =>
            PresentationScript(
              run:
                view.displayErrorMessage("Invalid bid.")
                view.displayShowInvalidBid()
              ,
              waitFor(3000),
              run(view.displayClearInvalidBid())
            )
          case error: GameError =>
            PresentationScript(run(view.displayErrorMessage(error.toString)))

  private def run(action: => Unit): PresentationStep = PresentationStep.run(action)

  private def waitFor(delayMs: Double): PresentationStep = PresentationStep.waitFor(delayMs)
