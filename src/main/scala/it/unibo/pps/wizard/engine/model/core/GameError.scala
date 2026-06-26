package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.Card

enum Reasons:
  case CardNotInHand
  case MustFollowLeader(requiredColor: Card.Color)

enum GameError:
  case NotYourTurn
  case InvalidBid
  case CardNotAllowed(reason: Reasons)
  case InvalidAction