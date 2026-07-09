package it.unibo.pps.wizard.application

import it.unibo.pps.wizard.application.WizardApplicationContext.WizardApplicationContextBuilder
import it.unibo.pps.wizard.application.gui.pages.MainPage
import it.unibo.pps.wizard.engine.ports.WizardInboundPort
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object WizardApplication extends JFXApp3:
  private val contextBuilder: WizardApplicationContextBuilder = WizardApplicationContext.builder

  given applicationContext: WizardApplicationContext = this.contextBuilder.build

  def launch(inboundPort: WizardInboundPort)(args: Array[String]): Unit =
    Future:
      this.contextBuilder.setInboundPort(inboundPort)
      this.main(args)

  override def start(): Unit =
    stageConfiguration()
    MainPage(this.stage)

  override def stopApp(): Unit =
    System.exit(0)

  private def stageConfiguration(): Unit =
    this.stage = new PrimaryStage():
      title = "PPS Card Game - Wizard"
      resizable = true
      minWidth = 1200
      minHeight = 800
    this.contextBuilder.setPrimaryStage(this.stage)
