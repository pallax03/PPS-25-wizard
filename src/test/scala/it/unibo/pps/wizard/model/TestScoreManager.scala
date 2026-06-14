package it.unibo.pps.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TestScoreManager extends AnyWordSpec with Matchers {

  // --- TEST FIXTURES ---
  val p1Id: PlayerId = 1
  val p2Id: PlayerId = 2
  val p3Id: PlayerId = 3

  val player1: Player = Player.human(p1Id, "Alice")
  val player2: Player = Player.human(p2Id, "Bob")
  val player3: Player = Player.human(p3Id, "Charlie")
  val mockPlayers: List[Player] = List(player1, player2, player3)

  val initialCore = CoreState(
    players = mockPlayers,
    deck = Deck(),
    round = Round(3),
    dealerId = p1Id
  )

  "The ScoreManager" when {

    "calculating round scores" should {

      "award 20 points flat if a player correctly bids and wins 0 tricks" in {
        // Setup scenario: Player 1 bids 0 and wins exactly 0 tricks
        val bids = BidsCollection.empty + (p1Id -> Bid(0))
        val tricksWon = BidsCollection.empty + (p1Id -> Bid(0))

        val scoringState = GameState.Scoring(initialCore, bids, tricksWon)
        val (finalScoreboard, _) = ScoreManager.processScores(scoringState, Scoreboard.empty)

        // Rule: Bidding 0 and getting 0 = 20 points base + (0 tricks * 10) = 20 points
        finalScoreboard.getPoints(p1Id) shouldBe 20
      }

      "award 20 points base plus 10 points per trick if a player hits their exact positive bid" in {
        // Setup scenario: Player 2 bids 2 and wins exactly 2 tricks
        val bids = BidsCollection.empty + (p2Id -> Bid(2))
        val tricksWon = BidsCollection.empty + (p2Id -> Bid(2))

        val scoringState = GameState.Scoring(initialCore, bids, tricksWon)
        val (finalScoreboard, _) = ScoreManager.processScores(scoringState, Scoreboard.empty)

        // Rule: Bidding 2 and getting 2 = 20 points base + (2 tricks * 10) = 40 points
        finalScoreboard.getPoints(p2Id) shouldBe 40
      }

      "deduct 10 points per trick of difference if a player wins MORE tricks than their bid" in {
        // Setup scenario: Player 3 bids 1, but accidentally wins 3 tricks (difference = 2)
        val bids = BidsCollection.empty + (p3Id -> Bid(1))
        val tricksWon = BidsCollection.empty + (p3Id -> Bid(3))

        val scoringState = GameState.Scoring(initialCore, bids, tricksWon)
        val (finalScoreboard, _) = ScoreManager.processScores(scoringState, Scoreboard.empty)

        // Rule: A difference of 2 tricks results in a -20 points penalty
        finalScoreboard.getPoints(p3Id) shouldBe -20
      }

      "deduct 10 points per trick of difference if a player wins FEWER tricks than their bid" in {
        // Setup scenario: Player 1 bids 3, but wins 0 tricks (difference = 3)
        val bids = BidsCollection.empty + (p1Id -> Bid(3))
        val tricksWon = BidsCollection.empty + (p1Id -> Bid(0))

        val scoringState = GameState.Scoring(initialCore, bids, tricksWon)
        val (finalScoreboard, _) = ScoreManager.processScores(scoringState, Scoreboard.empty)

        // Rule: A difference of 3 tricks results in a -30 points penalty
        finalScoreboard.getPoints(p1Id) shouldBe -30
      }

      "correctly accumulate points over existing scores in the scoreboard" in {
        // Setup scenario: Player 1 hits their exact bid of 1 trick -> earns +30 points
        val bids = BidsCollection.empty + (p1Id -> Bid(1))
        val tricksWon = BidsCollection.empty + (p1Id -> Bid(1))

        // Initialize a pre-existing scoreboard where Player 1 already has 50 points
        val currentScoreboard = Scoreboard.empty.updateScore(p1Id, 50)

        val scoringState = GameState.Scoring(initialCore, bids, tricksWon)
        val (finalScoreboard, _) = ScoreManager.processScores(scoringState, currentScoreboard)

        // Calculation: 50 (previous) + 30 (earned this round) = 80 total points
        finalScoreboard.getPoints(p1Id) shouldBe 80
      }

      "properly advance the round number and rotate the dealer for the next round" in {
        val bids = BidsCollection.empty
        val tricksWon = BidsCollection.empty
        val scoringState = GameState.Scoring(initialCore, bids, tricksWon)

        val (_, nextState) = ScoreManager.processScores(scoringState, Scoreboard.empty)

        // Verify the game transitions back to the Dealing phase
        nextState shouldBe a[GameState.Dealing]
        val dealingState = nextState.asInstanceOf[GameState.Dealing]

        // Verify that the round progresses from 3 to 4
        dealingState.core.round.toInt shouldBe 4
        // Verify that the dealer position rotates from Player 1 (p1Id) to Player 2 (p2Id)
        dealingState.core.dealerId shouldBe p2Id
      }
    }
  }
}