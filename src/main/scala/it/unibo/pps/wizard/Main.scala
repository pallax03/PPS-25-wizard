package it.unibo.pps.wizard

import io.vertx.core.Vertx
import it.unibo.pps.wizard.application.scalafx.WizardApplication
import it.unibo.pps.wizard.engine.adapters.VertxEventBusAdapter
import it.unibo.pps.wizard.engine.adapters.WizardGameAdapter
import it.unibo.pps.wizard.engine.adapters.WizardPrologAdapter
import it.unibo.pps.wizard.engine.ports.WizardAIPort
import it.unibo.pps.wizard.engine.ports.WizardInboundPort
import it.unibo.pps.wizard.engine.ports.WizardOutboundPort

object Main:

  def main(args: Array[String]): Unit =
    try
      println("Starting wizard system...")
      val vertx = Vertx.vertx()

      val wizardOutboundPort: WizardOutboundPort = VertxEventBusAdapter(vertx)

      val wizardEnginePort: WizardInboundPort = WizardGameAdapter(vertx, wizardOutboundPort)
      val wizardAIPort: WizardAIPort = WizardPrologAdapter(wizardEnginePort)

      println("Launching wizard application...")
      WizardApplication.launch(wizardEnginePort, wizardAIPort, vertx)(Array.empty)

    catch
      case error: Throwable =>
        println("Error during wizard system startup: " + error.getMessage)
        error.printStackTrace()
        System.exit(1)
