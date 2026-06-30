package it.unibo.pps.wizard

import io.vertx.core.{Future, Promise, Vertx}
import it.unibo.pps.wizard.application.WizardApplication
import it.unibo.pps.wizard.application.proxy.LocalWizardProxy
import it.unibo.pps.wizard.engine.services.WizardService

@main
def main(): Unit =
  deployServiceLocally()
    .map:
      deployApplicationLocally
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

def deployApplicationLocally(service: WizardService): Unit =
  service.localAdapter.foreach: localAdapter =>
    println("Deploying wizard application...")
    WizardApplication.launch(LocalWizardProxy(localAdapter.port))(Array.empty)
    println("Wizard application deployed.")