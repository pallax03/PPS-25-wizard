package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.{Card, PlayerId}

enum CardNotAllowedReasons:
  case CardNotInHand
  case MustFollowLeader(requiredColor: Card.Color)

enum InconsistentStateReasons:
  case TableNoWinner
  case HandNotFoundFor(playerId: PlayerId)

enum GameError:
  case NotYourTurn
  case InvalidBid
  case CardNotAllowed(reason: CardNotAllowedReasons)
  case InvalidAction
  case InconsistentState(reason: InconsistentStateReasons)
