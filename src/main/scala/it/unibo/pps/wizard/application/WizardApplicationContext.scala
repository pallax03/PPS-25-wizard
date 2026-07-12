package it.unibo.pps.wizard.application

import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardInboundPort}
import scalafx.stage.Stage

trait WizardApplicationContext:
  def primaryStage: Stage

  def inboundPort: WizardInboundPort
  def hintPort: WizardAIPort

object WizardApplicationContext:
  def apply(primaryStage: Stage, inboundPort: WizardInboundPort, hintPort: WizardAIPort): WizardApplicationContext =
    BasicWizardApplicationContext(primaryStage, inboundPort, hintPort)

  def builder: WizardApplicationContextBuilder = WizardApplicationContextBuilder()

  private case class BasicWizardApplicationContext(
      override val primaryStage: Stage,
      override val inboundPort: WizardInboundPort,
      override val hintPort: WizardAIPort,
  ) extends WizardApplicationContext

  case class WizardApplicationContextBuilder private[WizardApplicationContext] ():
    private var stage: Option[Stage] = Option.empty
    private var inboundPort: Option[WizardInboundPort] = Option.empty
    private var hintPort: Option[WizardAIPort] = Option.empty

    def setPrimaryStage(stage: Stage): this.type =
      this.stage = Option(stage)
      this

    def setInboundPort(inboundPort: WizardInboundPort): this.type =
      this.inboundPort = Option(inboundPort)
      this

    def setHintPort(hintPort: WizardAIPort): this.type =
      this.hintPort = Option(hintPort)
      this

    def build: WizardApplicationContext =
      WizardApplicationContext(
        this.stage.getOrElse:
          throw new IllegalStateException("Primary stage is not set"),
        this.inboundPort.getOrElse:
          throw new IllegalStateException("Inbound port is not set"),
        this.hintPort.getOrElse:
          throw new IllegalStateException("Hint port is not set"),
      )
