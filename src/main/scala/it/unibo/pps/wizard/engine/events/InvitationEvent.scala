package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.PlayerId
import it.unibo.pps.wizard.engine.model.view.{BidContext, PlayCardContext, TrumpContext}

sealed trait InvitationEvent extends WizardEvent, PlayerScoped

object InvitationEvent:
  case class WaitingForBid(playerId: PlayerId, context: BidContext) extends InvitationEvent
  case class WaitingForCard(playerId: PlayerId, context: PlayCardContext) extends InvitationEvent
  case class WaitingForTrump(playerId: PlayerId, context: TrumpContext) extends InvitationEvent
