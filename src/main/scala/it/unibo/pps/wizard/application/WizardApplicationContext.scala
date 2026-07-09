package it.unibo.pps.wizard.application

import it.unibo.pps.wizard.engine.ports.WizardInboundPort
import scalafx.stage.Stage

trait WizardApplicationContext:
  def primaryStage: Stage

  def inboundPort: WizardInboundPort

object WizardApplicationContext:
  def apply(primaryStage: Stage, inboundPort: WizardInboundPort): WizardApplicationContext =
    BasicWizardApplicationContext(primaryStage, inboundPort)

  def builder: WizardApplicationContextBuilder = WizardApplicationContextBuilder()

  private case class BasicWizardApplicationContext(
      override val primaryStage: Stage,
      override val inboundPort: WizardInboundPort
  ) extends WizardApplicationContext

  case class WizardApplicationContextBuilder private[WizardApplicationContext] ():
    private var stage: Option[Stage] = Option.empty
    private var inboundPort: Option[WizardInboundPort] = Option.empty

    def setPrimaryStage(stage: Stage): this.type =
      this.stage = Option(stage)
      this

    def setInboundPort(inboundPort: WizardInboundPort): this.type =
      this.inboundPort = Option(inboundPort)
      this

    def build: WizardApplicationContext =
      WizardApplicationContext(
        this.stage.getOrElse:
          throw new IllegalStateException("Primary stage is not set")
        ,
        this.inboundPort.getOrElse:
          throw new IllegalStateException("Inbound port is not set")
      )
