package it.unibo.pps.wizard.engine.ports

import it.unibo.pps.wizard.engine.events.Event
import it.unibo.pps.wizard.engine.model.basic.Players
import it.unibo.pps.wizard.engine.model.configuration.GameConfiguration
import it.unibo.pps.wizard.engine.model.core.GameAction
import it.unibo.pps.wizard.engine.model.game.WizardGameState

import scala.concurrent.Future
import scala.reflect.ClassTag

trait WizardPort:

  def getState: Future[WizardGameState]

  def startGame(players: Players, config: GameConfiguration): Future[Unit]

  def submitAction(action: GameAction): Future[Unit]

  def subscribe[T <: Event: ClassTag](handler: T => Unit): Future[String]

  def unsubscribe(subscriptionIds: String*): Future[Unit]
