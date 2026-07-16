package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.PlayerId
import it.unibo.pps.wizard.engine.model.basic.cards.Card

enum CardNotAllowedReasons(val legitCards: List[Card]):
  case CardNotInHand(cards: List[Card]) extends CardNotAllowedReasons(cards)
  case MustFollowColor(requiredColor: Card.Color, cards: List[Card])
      extends CardNotAllowedReasons(cards)

enum InconsistentStateReasons:
  case TableNoWinner
  case HandNotFoundFor(playerId: PlayerId)

enum GameError:
  case NotYourTurn
  case InvalidBid
  case CardNotAllowed(reason: CardNotAllowedReasons)
  case InvalidAction
  case InconsistentState(reason: InconsistentStateReasons)
