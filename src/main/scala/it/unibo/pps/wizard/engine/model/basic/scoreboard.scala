package it.unibo.pps.wizard.engine.model.basic

case class RoundRow(round: Round, playerStats: Map[PlayerId, Option[(Score, Bid)]]):
  def getScore(pId: PlayerId): String =
    playerStats.get(pId).flatten.map(data => data._1.value.toString).getOrElse("")
  def getBid(pId: PlayerId): String =
    playerStats.get(pId).flatten.map(data => data._2.value.toString).getOrElse("")

object RoundRow:
  private def calculateMaxRounds(numPlayers: Int): Int = 60 / numPlayers

  def getStatsForAllPlayers(
      round: Round,
      players: Players,
      sb: Scoreboard
  ): Map[PlayerId, Option[(Score, Bid)]] =
    players.toList.map { p =>
      val playerHistory = sb(p.id)

      p.id -> playerHistory.get(round)
    }.toMap

  def createRows(players: Players, sb: Scoreboard): List[RoundRow] =
    val maxRounds = calculateMaxRounds(players.toList.size)

    (1 to maxRounds).map { rNum =>
      val round = Round(rNum)
      RoundRow(round, getStatsForAllPlayers(round, players, sb))
    }.toList

opaque type Score = Int

object Score:
  def apply(points: Int): Score = points
  def zero: Score = 0

  extension (s: Score) def value: Int = s

opaque type Scoreboard = Map[PlayerId, Map[Round, (Score, Bid)]]

object Scoreboard:
  def empty: Scoreboard = Map.empty

  extension (sb: Scoreboard)
    def apply(p: PlayerId): Map[Round, (Score, Bid)] = sb.getOrElse(p, Map.empty)
    def addScore(p: PlayerId, round: Round, points: Score, bid: Bid): Scoreboard =
      sb.updated(p, sb.getOrElse(p, Map.empty).updated(round, (points, bid)))
    def getStatsForRound(r: Round, pId: PlayerId): (Score, Bid) =
      sb.getOrElse(pId, Map.empty).getOrElse(r, (Score(0), Bid(0)))
    private def allPlayedRounds: List[Round] = sb.values.flatMap(_.keys).toSet.toList.sorted
    def toRoundRows(players: Players): List[RoundRow] =
      sb.allPlayedRounds.map { round =>
        RoundRow(round, RoundRow.getStatsForAllPlayers(round, players, sb))
      }

given Ordering[Round] with
  def compare(x: Round, y: Round): Int = x.value.compare(y.value)
