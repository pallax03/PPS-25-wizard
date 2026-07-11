package it.unibo.pps.wizard.engine.ports

import it.unibo.pps.wizard.engine.model.basic.Card.Color
import it.unibo.pps.wizard.engine.model.basic.{PlayerId, Card, Bid}

import scala.concurrent.Future

trait WizardAIPort:
  def getResolvedTrumpColor(playerId: PlayerId): Future[Color]
  def getPlaceBid(playerId: PlayerId): Future[Bid]
  def getBestCard(playerId: PlayerId): Future[Card]
