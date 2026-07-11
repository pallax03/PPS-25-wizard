package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.Card

enum CardNotAllowedReasons(val legitCards: List[Card]):
  case CardNotInHand(cards: List[Card]) extends CardNotAllowedReasons(cards)
  case MustFollowColor(requiredColor: Card.Color, cards: List[Card])
      extends CardNotAllowedReasons(cards)
enum GameError:
  case NotYourTurn
  case InvalidBid
  case CardNotAllowed(reason: CardNotAllowedReasons)
  case InvalidAction
