package it.unibo.pps.wizard.engine.model.game

import io.vertx.core.Vertx
import io.vertx.core.eventbus.MessageConsumer
import it.unibo.pps.wizard.engine.events.Event
import it.unibo.pps.wizard.engine.model.configuration.GameConfiguration
import it.unibo.pps.wizard.engine.ports.WizardPort
import it.unibo.pps.wizard.util.vertx.VerticleExecutor

import scala.concurrent.Future
import scala.reflect.ClassTag

class WizardGame(private val vertx: Vertx) extends WizardPort:
  private var state: WizardGameState = WizardGameState.NotConfigured
  private val verticleExecutor: VerticleExecutor = VerticleExecutor(this.vertx)
  private var subscriptions: Map[String, MessageConsumer[?]] = Map.empty

  override def getState: Future[WizardGameState] = ???

  override def startGame(gameConfiguration: GameConfiguration): Future[Unit] = ???

  override def applyMove: Future[Unit] = ???

  override def subscribe[T <: Event : ClassTag](handler: T => Unit): Future[String] = ???

  override def unsubscribe(subscriptionIds: String*): Future[Unit] = ???