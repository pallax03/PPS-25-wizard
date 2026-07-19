package it.unibo.pps.wizard.application.scalafx

import io.vertx.core.Vertx
import it.unibo.pps.wizard.application.bot.BotLifecycleManager
import it.unibo.pps.wizard.engine.ports.WizardAIPort
import it.unibo.pps.wizard.engine.ports.WizardInboundPort
import scalafx.stage.Stage

/** Represents the context of the Wizard application, providing access to the primary stage, inbound port, hint port, and bot lifecycle manager. */
trait WizardApplicationContext:
  /**
   * Returns the primary stage of the application.
   *
   * @return the primary stage
   */
  def primaryStage: Stage

  /**
   * Returns the inbound port for receiving events.
   *
   * @return the inbound port
   */
  def inboundPort: WizardInboundPort

  /**
   * Returns the hint port for AI hints.
   *
   * @return the hint port
   */
  def hintPort: WizardAIPort

  /**
   * Returns the bot lifecycle manager.
   *
   * @return the bot lifecycle manager
   */
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

  /**
   * A private case class that implements the WizardApplicationContext trait.
   *
   * @param primaryStage the primary stage of the application
   * @param inboundPort the inbound port for receiving events
   * @param hintPort the hint port for AI hints
   * @param vertx the Vert.x instance used for event handling
   */
  private case class BasicWizardApplicationContext(
      override val primaryStage: Stage,
      override val inboundPort: WizardInboundPort,
      override val hintPort: WizardAIPort,
      vertx: Vertx
  ) extends WizardApplicationContext:

    override val botLifecycleManager: BotLifecycleManager =
      BotLifecycleManager(vertx, inboundPort, hintPort)

  /** A builder for creating instances of WizardApplicationContext. */
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
