package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.*

/**
 * Represents an explicit command or intention submitted by a player. * GameActions are the only
 * acceptable inputs that the [[GameEngine]] can process to advance the state of the game. They
 * represent attempts to alter the game state (e.g., playing a card, placing a bid) and can be
 * rejected if they violate game rules.
 */
enum GameAction:
  def playerId: PlayerId

  case PlaceBid(playerId: PlayerId, bid: Bid)
  case ChooseTrump(playerId: PlayerId, color: Card.Color)
  case PlayCard(playerId: PlayerId, card: Card)
