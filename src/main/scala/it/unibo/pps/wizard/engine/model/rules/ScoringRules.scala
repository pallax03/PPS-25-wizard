package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*

object ScoringRules:
  // Official Wizard rules scoring constants
  private final val BASE_WIN_POINTS = 20
  private final val POINTS_PER_TRICK = 10

  def compute(players: Players, bids: Bids, tricks: Tricks, scoreboard: Scoreboard): Scoreboard =
    players.toList.foldLeft(scoreboard): (sb, player) =>
      val points = bids(player.id).scoreAgainst(tricks(player.id))
      sb.updateScore(player.id, points)

  extension (bid: Bid)
    def scoreAgainst(tricksWon: Int): Int =
      if bid.value == tricksWon
      then BASE_WIN_POINTS + (tricksWon * POINTS_PER_TRICK)
      else -Math.abs(bid.value - tricksWon) * POINTS_PER_TRICK

// DSL SE ScoringRules OPERASSE SOLO DA CALCOLATORE DI PUNTI E LASCIASSE LA LOGICA DI AGGIORNAMENTO DELLO SCOREBOARD A ROUND MANAGER, SI POTREBBE FARE COSI':
//extension (bid: Bid)
//  def calculatePointsFor(tricksWon: Int): Int =
//    if bid.value == tricksWon
//    then BASE_WIN_POINTS + (tricksWon * POINTS_PER_TRICK)
//    else -Math.abs(bid.value - tricksWon) * POINTS_PER_TRICK

export ScoringRules.*
