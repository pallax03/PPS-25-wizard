package it.unibo.pps.wizard.engine.ports

import it.unibo.pps.wizard.engine.model.basic.cards.Card
import it.unibo.pps.wizard.engine.model.basic.{PlayerId, Bid}

import scala.concurrent.Future

trait WizardAIPort:
  def resolvedTrumpColor(playerId: PlayerId): Future[Card.Color]
  def placeBid(playerId: PlayerId): Future[Bid]
  def adjustBid(playerId: PlayerId): Future[Bid]
  def bestCard(playerId: PlayerId): Future[Card]
