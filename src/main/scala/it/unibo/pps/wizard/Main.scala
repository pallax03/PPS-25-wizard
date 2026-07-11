package it.unibo.pps.wizard

import io.vertx.core.Vertx
import it.unibo.pps.wizard.application.WizardApplication
import it.unibo.pps.wizard.application.bot.BotManagerVerticle
import it.unibo.pps.wizard.engine.adapters.{
  VertxEventBusAdapter,
  WizardGameAdapter,
  WizardPrologAdapter
}
import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardInboundPort, WizardOutboundPort}

object Main:

  def main(args: Array[String]): Unit =
    try
      println("Starting wizard system...")
      val vertx = Vertx.vertx()

      val wizardOutboundPort: WizardOutboundPort = VertxEventBusAdapter(vertx)

      val wizardEnginePort: WizardInboundPort = WizardGameAdapter(vertx, wizardOutboundPort)
      val wizardAIPort: WizardAIPort = WizardPrologAdapter(wizardEnginePort)

      vertx
        .deployVerticle(BotManagerVerticle(wizardEnginePort, wizardAIPort))
        .onSuccess(_ => println("Bot manager deployed."))
        .onFailure(err => println("Failed to deploy bot manager: " + err.getMessage))

      println("Launching wizard application...")
      WizardApplication.launch(wizardEnginePort)(Array.empty)

    catch
      case error: Throwable =>
        println("Error during wizard system startup: " + error.getMessage)
        error.printStackTrace()
        System.exit(1)
