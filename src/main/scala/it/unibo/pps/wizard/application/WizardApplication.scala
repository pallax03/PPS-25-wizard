package it.unibo.pps.wizard.application

import it.unibo.pps.wizard.application.WizardApplicationContext.WizardApplicationContextBuilder
import it.unibo.pps.wizard.application.gui.pages.MainMenuPage
import it.unibo.pps.wizard.engine.ports.WizardPort
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object WizardApplication extends JFXApp3:
  private val contextBuilder: WizardApplicationContextBuilder = WizardApplicationContext.builder

  given applicationContext: WizardApplicationContext = this.contextBuilder.build

  def launch(wizardEngineProxy: WizardPort)(args: Array[String]): Unit =
    Future:
      this.contextBuilder.setWizardEngineProxy(wizardEngineProxy)
      this.main(args)

  override def start(): Unit =
    stageConfiguration()
    MainMenuPage(this.stage)

  override def stopApp(): Unit =
    System.exit(0)

  private def stageConfiguration(): Unit =
    this.stage = new PrimaryStage():
      title = "PPS Card Game - Wizard"
      resizable = true
      minWidth = 1200
      minHeight = 800
    this.contextBuilder.setPrimaryStage(this.stage)
