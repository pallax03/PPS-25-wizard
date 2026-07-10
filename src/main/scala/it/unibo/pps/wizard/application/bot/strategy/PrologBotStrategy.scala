package it.unibo.pps.wizard.application.bot.strategy

import it.unibo.pps.wizard.application.bot.strategy.BotStrategy
import it.unibo.pps.wizard.engine.events.{FailureEvent, InvitationEvent}
import it.unibo.pps.wizard.engine.model.core.{GameAction, GameError}
import it.unibo.pps.wizard.engine.ports.WizardAIPort

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PrologBotStrategy(port: WizardAIPort) extends BotStrategy:

  override def resolveInvitationEvents(invitation: InvitationEvent): Future[GameAction] = invitation match
    case InvitationEvent.WaitingForCard(playerId, _) =>
      port.getBestCard(playerId).map(card => GameAction.PlayCard(playerId, card))
    case InvitationEvent.WaitingForBid(playerId, _) => ???
    case InvitationEvent.WaitingForTrump(playerId, _) => ???

  override def resolveFailedEvents(failure: FailureEvent): Future[GameAction] = failure match
    case FailureEvent.ActionFailed(playerId, reason) => reason match
      case GameError.InvalidBid => ???
      case GameError.CardNotAllowed(reason) => ???
      case _ => ???