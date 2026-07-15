package it.unibo.pps.wizard.application.scalafx

import io.vertx.core.Vertx
import it.unibo.pps.wizard.application.bot.BotLifecycleManager
import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardInboundPort}
import scalafx.stage.Stage

trait WizardApplicationContext:
  def primaryStage: Stage
  def inboundPort: WizardInboundPort
  def hintPort: WizardAIPort
  def botLifecycleManager: BotLifecycleManager

object WizardApplicationContext:
  def apply(
      primaryStage: Stage,
      inboundPort: WizardInboundPort,
      hintPort: WizardAIPort,
      vertx: Vertx
  ): WizardApplicationContext =
    BasicWizardApplicationContext(primaryStage, inboundPort, hintPort, vertx)

  def builder: WizardApplicationContextBuilder = WizardApplicationContextBuilder()

  private case class BasicWizardApplicationContext(
      override val primaryStage: Stage,
      override val inboundPort: WizardInboundPort,
      override val hintPort: WizardAIPort,
      vertx: Vertx
  ) extends WizardApplicationContext:

    override val botLifecycleManager: BotLifecycleManager =
      BotLifecycleManager(vertx, inboundPort, hintPort)

  case class WizardApplicationContextBuilder private[WizardApplicationContext] ():
    private var stage: Option[Stage] = Option.empty
    private var inboundPort: Option[WizardInboundPort] = Option.empty
    private var hintPort: Option[WizardAIPort] = Option.empty
    private var vertx: Option[Vertx] = Option.empty

    def setPrimaryStage(stage: Stage): this.type =
      this.stage = Option(stage)
      this

    def setInboundPort(inboundPort: WizardInboundPort): this.type =
      this.inboundPort = Option(inboundPort)
      this

    def setHintPort(hintPort: WizardAIPort): this.type =
      this.hintPort = Option(hintPort)
      this

    def setVertx(vertx: Vertx): this.type =
      this.vertx = Option(vertx)
      this

    def build: WizardApplicationContext =
      WizardApplicationContext(
        this.stage.getOrElse:
          throw new IllegalStateException("Primary stage is not set")
        ,
        this.inboundPort.getOrElse:
          throw new IllegalStateException("Inbound port is not set")
        ,
        this.hintPort.getOrElse:
          throw new IllegalStateException("Hint port is not set")
        ,
        this.vertx.getOrElse:
          throw new IllegalStateException("Vertx instance is not set")
      )
