package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.PlayerId

sealed trait FailureEvent extends WizardEvent

object FailureEvent:
  case class ActionFailed(playerId: PlayerId, reason: String) extends FailureEvent
