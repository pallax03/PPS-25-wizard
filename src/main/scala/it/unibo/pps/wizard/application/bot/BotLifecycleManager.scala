package it.unibo.pps.wizard.application.bot

import io.vertx.core.Vertx
import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardInboundPort}
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}

class BotLifecycleManager(
    private val vertx: Vertx,
    private val inboundPort: WizardInboundPort,
    private val aiPort: WizardAIPort
):
  private var currentBotDeploymentId: Option[String] = None

  def setupNewBotManager(): Future[Unit] =
    val promise = Promise[Unit]()
    
    val cleanupFuture: Future[Unit] = currentBotDeploymentId match
      case Some(oldId) =>
        val undeployPromise = Promise[Unit]()
        vertx.undeploy(oldId).onComplete: _ =>
          println(s"Old bot manager with deployment ID $oldId undeployed.")
          currentBotDeploymentId = None
          undeployPromise.success(())
        undeployPromise.future
      case None =>
        Future.successful(())
    
    cleanupFuture.onComplete:
      case Success(_) =>
        val newBotVerticle = BotManagerVerticle(inboundPort, aiPort)
        
        vertx.deployVerticle(newBotVerticle).onComplete: event =>
          if event.succeeded() then
            val newId = event.result()
            currentBotDeploymentId = Some(newId)
            println(s"New bot manager deployed with ID $newId.")
            promise.success(())
          else
            println(s"Failed to deploy new bot manager: ${event.cause().getMessage}")
            promise.failure(event.cause())
      case Failure(exception) =>
        println(s"Failed to clean up old bot manager: ${exception.getMessage}")
        promise.failure(exception)
    promise.future
      