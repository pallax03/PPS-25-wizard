package it.unibo.pps.wizard.engine.model.game

import it.unibo.pps.wizard.engine.model.core.GameState

enum WizardGameState:
  case NotConfigured
  case Running(state: GameState)
  case Finished
