package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TestScoringPhase extends AnyWordSpec with Matchers:
  val scoringRules: ScoringRules = ScoringRules()

  val p1Id: PlayerId = PlayerId(1)
  val p2Id: PlayerId = PlayerId(2)
  val p3Id: PlayerId = PlayerId(3)

  val player1: Player = Player.human(p1Id)
  val player2: Player = Player.human(p2Id)
  val player3: Player = Player.human(p3Id)
  val mockPlayers: List[Player] = List(player1, player2, player3)

  "The ScoringRules" when {

    "calculating round scores" should {

      "award 20 points flat if a player correctly bids and wins 0 tricks" in {
        val bids = BidsCollection.empty + (p1Id -> Bid(0))

        val tricksWon = TricksWon.initialize(mockPlayers)

        val finalScoreboard = scoringRules.processScores(mockPlayers, bids, tricksWon, Scoreboard.empty)

        finalScoreboard.getPoints(p1Id) shouldBe 20
      }

      "award 20 points base plus 10 points per trick if a player hits their exact positive bid" in {
        val bids = BidsCollection.empty + (p2Id -> Bid(2))

        val tricksWon: TricksWon = TricksWon(Map(p2Id -> 2))

        val finalScoreboard = scoringRules.processScores(mockPlayers, bids, tricksWon, Scoreboard.empty)

        finalScoreboard.getPoints(p2Id) shouldBe 40
      }

      "deduct 10 points per trick of difference if a player wins MORE tricks than their bid" in {
        val bids = BidsCollection.empty + (p3Id -> Bid(1))
        val tricksWon: TricksWon = TricksWon(Map(p3Id -> 3))

        val finalScoreboard = scoringRules.processScores(mockPlayers, bids, tricksWon, Scoreboard.empty)

        finalScoreboard.getPoints(p3Id) shouldBe -20
      }

      "deduct 10 points per trick of difference if a player wins FEWER tricks than their bid" in {
        val bids = BidsCollection.empty + (p1Id -> Bid(3))
        val tricksWon: TricksWon = TricksWon.initialize(mockPlayers)

        val finalScoreboard = scoringRules.processScores(mockPlayers, bids, tricksWon, Scoreboard.empty)

        finalScoreboard.getPoints(p1Id) shouldBe -30
      }

      "correctly accumulate points over existing scores in the scoreboard" in {
        val bids = BidsCollection.empty + (p1Id -> Bid(1))
        val tricksWon: TricksWon = TricksWon(Map(p1Id -> 1))

        val currentScoreboard = Scoreboard.empty.updateScore(p1Id, 50)

        val finalScoreboard = scoringRules.processScores(mockPlayers, bids, tricksWon, currentScoreboard)

        finalScoreboard.getPoints(p1Id) shouldBe 80
      }
    }
  }