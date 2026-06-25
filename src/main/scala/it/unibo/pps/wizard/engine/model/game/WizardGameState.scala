package it.unibo.pps.wizard.engine.model.game

import it.unibo.pps.wizard.engine.model.core.GameState

enum WizardGameState:
  case NotConfigured // Initial State
  case Running(state: GameState)
  case Finished // see GameState -> Finished must be trigger when Scoring and round == RoundManager.MaxRound
