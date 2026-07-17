package it.unibo.pps.wizard.engine.model.basic

import it.unibo.pps.wizard.engine.model.basic.gameplay.Round

/**
 * Represents a row of statistics for a specific round, mapping each player to their
 * optional score and bid.
 *
 * @param round the game round these statistics refer to.
 * @param playerStats a map linking each player to their optional score and bid for this round.
 */
case class RoundRow(round: Round, playerStats: Map[PlayerId, Option[(Score, Bid)]]):
  /**
   * Returns the score of the player as a string.
   *
   * @param pId the ID of the player.
   * @return the score string, or an empty string if not present.
   */
  def getScore(pId: PlayerId): String =
    playerStats.get(pId).flatten.map(data => data._1.value.toString).getOrElse("")

  /**
   * Returns the bid of the player as a string.
   *
   * @param pId the ID of the player.
   * @return the bid string, or an empty string if not present.
   */
  def getBid(pId: PlayerId): String =
    playerStats.get(pId).flatten.map(data => data._2.toString).getOrElse("")

object RoundRow:
  private def calculateMaxRounds(numPlayers: Int): Int = 60 / numPlayers

  /**
   * Retrieves the score and bid stats for all players in a specific round.
   *
   * @param round   the target game round.
   * @param players the list of players.
   * @param sb      the scoreboard containing game history.
   * @return a map of player IDs to their optional round stats.
   */
  def getStatsForAllPlayers(
      round: Round,
      players: Players,
      sb: Scoreboard
  ): Map[PlayerId, Option[(Score, Bid)]] =
    players.toList.map { p =>
      val playerHistory = sb(p.id)
      p.id -> playerHistory.get(round)
    }.toMap

  /**
   * Generates and updates all round rows up to the maximum playable rounds.
   *
   * @param players the list of players.
   * @param sb      the current scoreboard.
   * @return a list of updated [[RoundRow]]s.
   */
  def updateRows(players: Players, sb: Scoreboard): List[RoundRow] =
    val maxRounds = calculateMaxRounds(players.toList.size)

    (1 to maxRounds).map { rNum =>
      val round = Round(rNum)
      RoundRow(round, getStatsForAllPlayers(round, players, sb))
    }.toList

  /**
   * Initializes all round rows with empty statistics for each player.
   *
   * @param players the list of players.
   * @return a list of empty [[RoundRow]]s for the entire game.
   */
  def initRows(players: Players): List[RoundRow] =
    val maxRounds = calculateMaxRounds(players.toList.size)

    (1 to maxRounds).map { rNum =>
      val round = Round(rNum)
      RoundRow(round, players.toList.map(p => p.id -> None).toMap)
    }.toList

/** Represents the score accumulated by a player. */
opaque type Score = Int
object Score:
  def apply(points: Int): Score = points
  def zero: Score = 0

  extension (s: Score) def value: Int = s

/**
 * Represents the history of scores and bids for all players across game rounds.
 * Maps each [[PlayerId]] to another map of [[Round]] to their corresponding [[Score]] and [[Bid]].
 */
opaque type Scoreboard = Map[PlayerId, Map[Round, (Score, Bid)]]
object Scoreboard:
  def empty: Scoreboard = Map.empty

  extension (sb: Scoreboard)
    /**
     * Returns the round-by-round history for a specific player.
     *
     * @param p the target player ID.
     * @return a map of rounds to the player's score and bid.
     */
    def apply(p: PlayerId): Map[Round, (Score, Bid)] = sb.getOrElse(p, Map.empty)

    /**
     * Records a score and a bid for a player in a specific round.
     *
     * @param p      the player ID.
     * @param round  the current round.
     * @param points the score obtained in the round.
     * @param bid    the bid placed for the round.
     * @return the updated [[Scoreboard]].
     */
    def addScore(p: PlayerId, round: Round, points: Score, bid: Bid): Scoreboard =
      sb.updated(p, sb.getOrElse(p, Map.empty).updated(round, (points, bid)))

    /**
     * Returns the score and bid of a player for a specific round.
     *
     * @param r   the target round.
     * @param pId the player ID.
     * @return a tuple of [[Score]] and [[Bid]], defaulting to (0, 0) if not found.
     */
    def getStatsForRound(r: Round, pId: PlayerId): (Score, Bid) =
      sb.getOrElse(pId, Map.empty).getOrElse(r, (Score(0), Bid(0)))

    private def allPlayedRounds: List[Round] = sb.values.flatMap(_.keys).toSet.toList.sorted

    /**
     * Converts the current scoreboard data into a list of round rows.
     *
     * @param players the list of players.
     * @return a list of [[RoundRow]]s containing stats for all played rounds.
     */
    def toRoundRows(players: Players): List[RoundRow] =
      sb.allPlayedRounds.map { round =>
        RoundRow(round, RoundRow.getStatsForAllPlayers(round, players, sb))
      }

given Ordering[Round] with
  def compare(x: Round, y: Round): Int = x.value.compare(y.value)
