package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.{Players, Scoreboard}

sealed trait LifecycleEvent extends WizardEvent

object LifecycleEvent:
  case class GameStarted(players: Players) extends LifecycleEvent
  case class GameEnded(finalScores: Scoreboard) extends LifecycleEvent
