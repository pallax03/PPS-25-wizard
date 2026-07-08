package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.*

case class CoreState(
    players: Players,
    hands: Hands,
    deck: Deck,
    trump: Trump,
    round: Round,
    dealerId: PlayerId,
    scoreboard: Scoreboard
):
  def updateTrump(trump: Trump): CoreState = this.copy(trump= trump)
