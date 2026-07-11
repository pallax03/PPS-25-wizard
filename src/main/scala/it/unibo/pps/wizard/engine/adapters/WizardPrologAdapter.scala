package it.unibo.pps.wizard.engine.adapters

import it.unibo.pps.wizard.engine.model.basic.{Bid, Card, PlayerId}
import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardInboundPort}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WizardPrologAdapter(private val inboundPort: WizardInboundPort) extends WizardAIPort:

  override def getResolvedTrumpColor(playerId: PlayerId): Future[Card.Color] = ???

  override def getPlaceBid(playerId: PlayerId): Future[Bid] = ???

  override def getBestCard(playerId: PlayerId): Future[Card] =
    inboundPort.getState.map:
      case WizardGameState.Running(state) => ???
      case _ =>
        throw IllegalStateException("Game is not running")
