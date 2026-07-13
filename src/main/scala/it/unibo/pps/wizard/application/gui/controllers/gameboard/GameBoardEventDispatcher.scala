package it.unibo.pps.wizard.application.gui.controllers.gameboard

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.engine.events.{ActionEvent, InvitationEvent, ProgressEvent}
import scalafx.application.Platform

class GameBoardEventDispatcher(private val view: GameBoardView)(using
    context: WizardApplicationContext
):
  def startListening(): Unit =
    context.inboundPort.subscribe[ActionEvent]:
      case ActionEvent.CardPlayed(playerId, card) => runOnUi(view.displayCardPlayed(playerId, card))
      case ActionEvent.TrumpColorResolved(playerId, color) =>
        runOnUi(view.displayTrumpSelected(playerId, color))
      case ActionEvent.BidPlaced(playerId, bid) => runOnUi(view.displayBidPlaced(playerId, bid))

    context.inboundPort.subscribe[ProgressEvent]:
      case ProgressEvent.CardsDealt(playerId, hands, trump, round) =>
        runOnUi(view.displayCardsDealt(playerId, hands, trump, round))
      case ProgressEvent.TrickWon(winnerId, trickedCards) =>
        runOnUi(view.displayTrickWon(winnerId, trickedCards))
      case ProgressEvent.RoundScored(scoreboard) => runOnUi(view.displayRoundScored(scoreboard))
      case ProgressEvent.PhaseChanged(phase)     => runOnUi(view.displayPhaseChanged(phase))
      case ProgressEvent.IsTurnOf(playerId, phase)      => runOnUi(view.displayTurnChanged(playerId, phase))

    context.inboundPort.subscribe[InvitationEvent]:
      case InvitationEvent.WaitingForTrump(playerId) =>
        runOnUi(view.displayWaitingForTrump(playerId))
      case _ =>

  private def runOnUi(action: => Unit): Unit =
    Platform.runLater(action)

//  private val actionRouting: PartialFunction[Event, Unit] =
//    case ActionEvent.CardPlayed(playerId, card) => view.displayCardPlayed(playerId, card)
//    case ActionEvent.TrumpColorResolved(playerId, color) => view.displayTrumpSelected(playerId, color)
//    case ActionEvent.BidPlaced(playerId, bid) => view.displayBidPlaced(playerId, bid)
//
//  private val progressRouting: PartialFunction[Event, Unit] =
//    case ProgressEvent.CardsDealt(playerId, hands, trump, round) =>
//      view.displayCardsDealt(playerId, hands, trump, round)
//    case ProgressEvent.TrickWon(winnerId, trickedCards) =>
//      view.displayTrickWon(winnerId, trickedCards)
//    case ProgressEvent.RoundScored(scoreboard) => ???
//    case ProgressEvent.PhaseChanged(phase) => view.displayPhaseChanged(phase)
//
//  private val invitationEvent: PartialFunction[Event, Unit] =
//    case InvitationEvent.WaitingForTrump(ctx) => view.displayWaitingForTrump(ctx.playerId)
//
//  private val eventRouter: PartialFunction[Event, Unit] =
//    actionRouting orElse progressRouting orElse invitationEvent
//
//  def startListening(): Unit =
//    context.inboundPort.subscribe[Event]: event =>
//      eventRouter.andThen(runOnUi).applyOrElse(event, _ => ())
//
//  private def runOnUi(action: Unit): Unit =
//    Platform.runLater(() => action)
