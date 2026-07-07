package it.unibo.pps.wizard.application.bot

import io.vertx.core.AbstractVerticle
import it.unibo.pps.wizard.engine.events.InvitationEvent
import it.unibo.pps.wizard.engine.ports.WizardPort

class BotManagerVerticle(wizardPort: WizardPort) extends AbstractVerticle:
  override def start(): Unit =
    wizardPort.subscribe[InvitationEvent]:
      case InvitationEvent.WaitingForBid(_)   => ()
      case InvitationEvent.WaitingForCard(_)  => ()
      case InvitationEvent.WaitingForTrump(_) => ()
