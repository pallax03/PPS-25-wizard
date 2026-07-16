package it.unibo.pps.wizard.application.scalafx

import io.vertx.core.Vertx
import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext.WizardApplicationContextBuilder
import it.unibo.pps.wizard.application.scalafx.pages.MainPage
import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardInboundPort}
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage

/**
 * The WizardApplication object is the entry point of the Wizard game application.
 * It extends JFXApp3 to provide a JavaFX application framework.
 */
object WizardApplication extends JFXApp3:
  private val contextBuilder: WizardApplicationContextBuilder = WizardApplicationContext.builder

  given applicationContext: WizardApplicationContext = this.contextBuilder.build

  /**
   * Launches the Wizard application with the specified inbound and hint ports, and the Vert.x instance.
   *
   * @param inboundPort the inbound port for receiving events
   * @param hintPort the hint port for AI hints
   * @param vertx the Vert.x instance used for event handling
   * @param args command-line arguments passed to the application
   */
  def launch(inboundPort: WizardInboundPort, hintPort: WizardAIPort, vertx: Vertx)(
      args: Array[String]
  ): Unit =
    this.contextBuilder.setInboundPort(inboundPort)
    this.contextBuilder.setHintPort(hintPort)
    this.contextBuilder.setVertx(vertx)
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
