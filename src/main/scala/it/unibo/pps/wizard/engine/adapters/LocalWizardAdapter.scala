package it.unibo.pps.wizard.engine.adapters

import it.unibo.pps.wizard.engine.ports.WizardPort

class LocalWizardAdapter(override val port: WizardPort) extends Adapter[WizardPort]
  