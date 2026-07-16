package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.basic.cards.{Hands, Card}
import it.unibo.pps.wizard.engine.model.basic.gameplay.{Round, Trump}

sealed trait ProgressEvent extends WizardEvent

object ProgressEvent:
  case class CardsDealt(playerId: PlayerId, hands: Hands, trump: Trump, round: Round)
      extends ProgressEvent
  case class TrickWon(winnerId: PlayerId, tricksWon: Trick, trickedCards: List[Card])
      extends ProgressEvent
  case class RoundScored(scoreboard: Scoreboard, players: Players) extends ProgressEvent
  case class PhaseChanged(phaseName: String) extends ProgressEvent
