package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.PlayerId
import it.unibo.pps.wizard.engine.model.view.{BidContext, PlayCardContext, TrumpContext}

sealed trait InvitationEvent extends WizardEvent:
  def playerId: PlayerId

object InvitationEvent:
  case class WaitingForBid(context: BidContext) extends InvitationEvent:
    override def playerId: PlayerId = context.playerId

  case class WaitingForCard(context: PlayCardContext) extends InvitationEvent:
    override def playerId: PlayerId = context.playerId

  case class WaitingForTrump(context: TrumpContext) extends InvitationEvent:
    override def playerId: PlayerId = context.playerId
