package it.unibo.pps.wizard.engine.ports

import it.unibo.pps.wizard.engine.adapters.WizardGameState
import it.unibo.pps.wizard.engine.events.Event
import it.unibo.pps.wizard.engine.model.basic.Players
import it.unibo.pps.wizard.engine.model.configuration.GameConfiguration
import it.unibo.pps.wizard.engine.model.core.GameAction

import scala.concurrent.Future
import scala.reflect.ClassTag

trait WizardInboundPort:

  def getState: Future[WizardGameState]

  def startGame(players: Players, config: GameConfiguration): Future[Unit]

  def submitAction(action: GameAction): Future[Unit]

  def subscribe[T <: Event: ClassTag](handler: T => Unit): Future[String]

  def unsubscribe(subscriptionIds: String*): Future[Unit]
