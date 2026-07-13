package it.unibo.pps.wizard.application.scalafx

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext.WizardApplicationContextBuilder
import it.unibo.pps.wizard.application.scalafx.pages.MainPage
import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardInboundPort}
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage

object WizardApplication extends JFXApp3:
  private val contextBuilder: WizardApplicationContextBuilder = WizardApplicationContext.builder

  given applicationContext: WizardApplicationContext = this.contextBuilder.build

  def launch(inboundPort: WizardInboundPort, hintPort: WizardAIPort)(args: Array[String]): Unit =
    this.contextBuilder.setInboundPort(inboundPort)
    this.contextBuilder.setHintPort(hintPort)
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
