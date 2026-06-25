package it.unibo.pps.wizard.application.proxy

import it.unibo.pps.wizard.engine.ports.WizardPort

case class LocalWizardProxy(private val port: WizardPort) extends WizardPort:



  export port.*
