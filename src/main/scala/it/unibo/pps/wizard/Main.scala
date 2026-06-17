package it.unibo.pps.wizard

import io.vertx.core.{Future, Promise, Vertx}
import it.unibo.pps.wizard.engine.services.WizardService

@main
def main(): Unit =
  deployServiceLocally()
    .onFailure: error =>
      error.printStackTrace()
      System.exit(1)

def deployServiceLocally(): Future[WizardService] =
  println("Deploying wizard engine service...")
  val vertx = Vertx.vertx()
  val service = WizardService()
  val serviceDeployed: Promise[WizardService] = Promise.promise()
  vertx
    .deployVerticle(service)
    .onSuccess: _ =>
      println("Wizard engine service deployed.")
      serviceDeployed.complete(service)
    .onFailure: error =>
      println("Failed to deploy wizard engine service.")
      serviceDeployed.fail(error)
  serviceDeployed.future()