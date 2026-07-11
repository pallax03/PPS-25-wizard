package it.unibo.pps.wizard.engine.events

trait WizardEvent extends Event

import it.unibo.pps.wizard.engine.model.basic.PlayerId

trait PlayerScoped extends WizardEvent:
  def playerId: PlayerId
