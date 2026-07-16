package it.unibo.pps.wizard.application.bot

import io.vertx.core.Vertx
import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardInboundPort}
import scala.concurrent.{Future, Promise}

class BotLifecycleManager(
    private val vertx: Vertx,
    private val inboundPort: WizardInboundPort,
    private val aiPort: WizardAIPort
):
  private var currentBotDeploymentId: Option[String] = None

  def setupNewBotManager(): Future[Unit] =
    val promise = Promise[Unit]()
    val newBotVerticle = BotManagerVerticle(inboundPort, aiPort)

    vertx
      .deployVerticle(newBotVerticle)
      .onComplete: event =>
        if event.succeeded() then
          val newId = event.result()
          currentBotDeploymentId = Some(newId)
          println(s"New bot manager deployed with ID $newId.")
          promise.success(())
        else
          println(s"Failed to deploy new bot manager: ${event.cause().getMessage}")
          promise.failure(event.cause())
    promise.future

  def shutdownBotManager(): Future[Unit] =
    currentBotDeploymentId match
      case Some(deploymentId) =>
        val promise = Promise[Unit]()
        vertx
          .undeploy(deploymentId)
          .onComplete: event =>
            if event.succeeded() then
              println(s"Bot manager with deployment ID $deploymentId undeployed.")
              currentBotDeploymentId = None
              promise.success(())
            else
              println(s"Failed to undeploy bot manager: ${event.cause().getMessage}")
              promise.failure(event.cause())
        promise.future
      case None =>
        println("No bot manager is currently deployed.")
        Future.successful(())
