package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.PlayerId
import it.unibo.pps.wizard.engine.model.core.GameError

sealed trait FailureEvent extends WizardEvent, PlayerScoped

object FailureEvent:
  case class ActionFailed(playerId: PlayerId, reason: GameError) extends FailureEvent
