package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameState

sealed trait InvitationEvent extends WizardEvent

object InvitationEvent:
  case class WaitingForBid(playerId: PlayerId) extends InvitationEvent
  case class WaitingForCard(playerId: PlayerId) extends InvitationEvent
  case class WaitingForTrump(playerId: PlayerId) extends InvitationEvent

  def fromState(state: GameState): Option[InvitationEvent] = state match
    case GameState.Bidding(_, Trump.WizardUnresolved(_), _, playerId) => Some(WaitingForTrump(playerId))
    case GameState.Bidding(_, _, _, playerId)                         => Some(WaitingForBid(playerId))
    case GameState.Playing(_, _, _, _, playerId, _)                   => Some(WaitingForCard(playerId))
    case GameState.Ended(_)                                           => None
