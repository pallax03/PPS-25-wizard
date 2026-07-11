package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.{Card, PlayerId}

sealed trait InvitationEvent extends WizardEvent, PlayerScoped

object InvitationEvent:
  case class WaitingForBid(playerId: PlayerId) extends InvitationEvent
  case class WaitingForCard(playerId: PlayerId, legalCards: List[Card]) extends InvitationEvent
  case class WaitingForTrump(playerId: PlayerId) extends InvitationEvent
