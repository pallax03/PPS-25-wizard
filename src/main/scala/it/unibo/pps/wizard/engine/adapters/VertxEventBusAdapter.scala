package it.unibo.pps.wizard.engine.adapters

import io.vertx.core.Vertx
import it.unibo.pps.wizard.engine.events.Event.addressOf
import it.unibo.pps.wizard.engine.events.{ActionEvent, FailureEvent, InvitationEvent, LifecycleEvent, ProgressEvent, WizardEvent}
import it.unibo.pps.wizard.engine.ports.WizardOutboundPort

import scala.concurrent.Future

class VertxEventBusAdapter(private val vertx: Vertx) extends WizardOutboundPort:

  override def publishEvent(event: WizardEvent): Future[Unit] =
    eventAddresses(event).foreach: address =>
      this.vertx.eventBus().publish(address, event)
    Future.successful(())

  override def publishAllEvents(events: List[WizardEvent]): Future[Unit] =
    events.foreach(publishEvent)
    Future.successful(())

  private def eventAddresses(event: WizardEvent): List[String] =
    val familyAddress = event match
      case _: ActionEvent     => addressOf[ActionEvent]
      case _: FailureEvent    => addressOf[FailureEvent]
      case _: InvitationEvent => addressOf[InvitationEvent]
      case _: LifecycleEvent  => addressOf[LifecycleEvent]
      case _: ProgressEvent   => addressOf[ProgressEvent]
    List(event.getClass.getSimpleName, familyAddress, addressOf[WizardEvent]).distinct
