package it.unibo.pps.wizard.engine.adapters

import io.vertx.core.Vertx
import it.unibo.pps.wizard.engine.model.basic.{Bid, Card, PlayerId}
import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardOutboundPort}
import it.unibo.pps.wizard.util.PrologEngine
import it.unibo.tuprolog.theory.Theory

import scala.concurrent.Future

class WizardPrologAdapter(private val vertx: Vertx, private val outboundPort: WizardOutboundPort)
    extends WizardAIPort:
  val prologEngine = PrologEngine.buildEngine(defineTheory)
  
  private def defineTheory: Theory = ???
  
  override def getResolvedTrumpColor(playerId: PlayerId): Future[Card.Color] = ???

  override def getPlaceBid(playerId: PlayerId): Future[Bid] = ???

  override def getBestCard(playerId: PlayerId): Future[Card] = ???
  