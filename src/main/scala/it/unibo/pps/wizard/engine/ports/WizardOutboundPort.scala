package it.unibo.pps.wizard.engine.ports

import it.unibo.pps.wizard.engine.events.WizardEvent
import scala.concurrent.Future

trait WizardOutboundPort:

  def publishEvent(event: WizardEvent): Future[Unit]

  def publishAllEvents(events: List[WizardEvent]): Future[Unit]
