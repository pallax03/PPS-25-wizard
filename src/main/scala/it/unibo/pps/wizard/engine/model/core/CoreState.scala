package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.rules.RoundManager.firstPlayer

case class CoreState(
                      players: Players,
                      hands: Hands,
                      trump: Trump,
                      round: Round,
                      dealerId: PlayerId,
                      scoreboard: Scoreboard
                    ):
  def updateTrump(trump: Trump): CoreState = this.copy(trump = trump)

object CoreState:
  def initialize(
                  players: Players,
                  round: Round,
                ): CoreState =
    CoreState(
      players = players,
      hands = Hands.empty,
      trump = Trump.Absent,
      round = round,
      dealerId = round.firstPlayer(players),
      scoreboard = Scoreboard.empty
    )