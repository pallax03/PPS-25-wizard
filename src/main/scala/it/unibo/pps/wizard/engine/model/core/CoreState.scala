package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.rules.*

case class CoreState(
                      players: List[Player],
                      hands: Hands,
                      deck: Deck,
                      round: Round,
                      dealerId: PlayerId,
                      scoreboard: Scoreboard
                    )