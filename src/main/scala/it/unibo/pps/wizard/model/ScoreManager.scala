package it.unibo.pps.wizard.model

/**
 * Manager responsible for calculating player scores at the end of each round
 * and updating the game's global scoreboard and state according to the official Wizard rules.
 */
object ScoreManager:

  // Official Wizard rules scoring constants
  private final val BASE_WIN_POINTS = 20
  private final val POINTS_PER_TRICK = 10
  private final val PENALTY_PER_TRICK_DIFF = 10

  /**
   * Processes the scores for the round that has just concluded, updates the cumulative scoreboard,
   * and synchronizes each player's internal history within the CoreState.
   *
   * @param state              The current [[GameState.Scoring]] containing the core state, player bids, and tricks won.
   * @param currentScoreboard  The global [[Scoreboard]] containing the accumulated points up to the current round.
   * @return                   A tuple containing the updated [[Scoreboard]] and the next [[GameState]]
   *                           (typically transitioning back to [[GameState.Dealing]] for the next round).
   */
  def processScores(state: GameState.Scoring, currentScoreboard: Scoreboard): (Scoreboard, GameState) =
    val core = state.core
    val bids = state.bids
    val tricksWon = state.bidsWon
    val underlyingTricks = tricksWon.asInstanceOf[Map[PlayerId, Bid]]

    // 1. Compute round stats and points for each player in a single pass
    val playersRoundData = core.players.map { player =>
      val playerId = player.id
      val bid = bids.getBid(playerId).map(_.toInt).getOrElse(0)
      val won = underlyingTricks.get(playerId).map(_.toInt).getOrElse(0)
      val roundPoints = calculatePlayerScore(bid, won)

      (player, bid, won, roundPoints)
    }

    // 2. Accumulate points into the global scoreboard using the pre-calculated round points
    val updatedScoreboard = playersRoundData.foldLeft(currentScoreboard) { (sb, data) =>
      val (player, _, _, roundPoints) = data
      sb.updateScore(player.id, roundPoints)
    }

    // 3. Update player entities with their fresh history and current cumulative score
    val updatedPlayers = playersRoundData.map { (player, bid, won, _) =>
      player.copy(
        bid = Some(bid),
        tricksWon = won,
        points = updatedScoreboard.getPoints(player.id)
      )
    }

    // 4. Prepare the transition to the next round by incrementing the Round number and rotating the Dealer
    val nextRound = core.round.next
    val nextDealer = nextDealerId(core.dealerId, core.players)

    val newCoreState = core.copy(
      players = updatedPlayers,
      round = nextRound,
      dealerId = nextDealer
    )

    (updatedScoreboard, GameState.Dealing(newCoreState))

  /**
   * Pure functional logic to compute a single player's round score based on official Wizard guidelines.
   * If the player accurately predicts their tricks: $+20$ points base, plus $+10$ points per trick won.
   * If the player misses their prediction: $-10$ points penalty per trick of difference (absolute value).
   *
   * @param bid  The number of tricks predicted by the player.
   * @param won  The actual number of tricks won by the player.
   * @return     The total positive or negative integer points scored during this specific round.
   */
  private def calculatePlayerScore(bid: Int, won: Int): Int =
    if bid == won then
      BASE_WIN_POINTS + (won * POINTS_PER_TRICK)
    else
      val difference = Math.abs(bid - won)
      -(difference * PENALTY_PER_TRICK_DIFF)

  /**
   * Utility method to determine the next dealer ID sequentially following a clockwise rotation.
   *
   * @param currentDealer  The [[PlayerId]] of the dealer from the round that just ended.
   * @param players        The full list of active [[Player]] entities participating in the game.
   * @return               The [[PlayerId]] of the next designated dealer.
   */
  private def nextDealerId(currentDealer: PlayerId, players: List[Player]): PlayerId =
    val index = players.indexWhere(_.id == currentDealer)
    players((index + 1) % players.size).id