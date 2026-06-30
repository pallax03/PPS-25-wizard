package it.unibo.pps.wizard.application

import it.unibo.pps.wizard.engine.ports.WizardPort
import scalafx.stage.Stage

trait WizardApplicationContext:
  def primaryStage: Stage

  def wizardEngineProxy: WizardPort

object WizardApplicationContext:
  def apply(primaryStage: Stage, wizardEngineProxy: WizardPort): WizardApplicationContext =
    BasicWizardApplicationContext(primaryStage, wizardEngineProxy)

  def builder: WizardApplicationContextBuilder = WizardApplicationContextBuilder()

  private case class BasicWizardApplicationContext(
    override val primaryStage: Stage,
    override val wizardEngineProxy: WizardPort
  ) extends WizardApplicationContext

  case class WizardApplicationContextBuilder private[WizardApplicationContext] ():
    private var stage: Option[Stage] = Option.empty
    private var port: Option[WizardPort] = Option.empty

    def setPrimaryStage(stage: Stage): this.type =
      this.stage = Option(stage)
      this

    def setWizardEngineProxy(port: WizardPort): this.type =
      this.port = Option(port)
      this

    def build: WizardApplicationContext =
      WizardApplicationContext(
        this.stage.getOrElse:
          throw new IllegalStateException("Primary stage is not set"),
        this.port.getOrElse:
          throw new IllegalStateException("Wizard engine proxy is not set")
      )