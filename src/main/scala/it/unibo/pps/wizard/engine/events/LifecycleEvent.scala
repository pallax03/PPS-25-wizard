package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.Scoreboard
import it.unibo.pps.wizard.engine.model.core.GameState

sealed trait LifecycleEvent extends WizardEvent

object LifecycleEvent:
  case class GameStarted(initialState: GameState) extends LifecycleEvent
  case class GameEnded(finalScores: Scoreboard) extends LifecycleEvent
