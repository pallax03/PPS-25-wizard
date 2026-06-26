package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TestRoundManager extends AnyWordSpec with Matchers:
  val roundManager: RoundManager = RoundManager()

  val p1Id: PlayerId = PlayerId(1)
  val p2Id: PlayerId = PlayerId(2)
  val p3Id: PlayerId = PlayerId(3)

  val mockPlayers: List[Player] = List(
    Player.human(p1Id),
    Player.human(p2Id),
    Player.human(p3Id)
  )
  val totalPlayers: Int = mockPlayers.size

  "The RoundManager" when {

    "validating a bidding turn" should {

      "accept the action if the action player is the one whose turn it currently is" in {
        val result = roundManager.validateBiddingTurn(actionPlayer = p1Id, currentPlayerTurn = p1Id)
        result shouldBe Right(())
      }

      "reject the action with NotYourTurn if a player tries to bid out of order" in {
        val result = roundManager.validateBiddingTurn(actionPlayer = p2Id, currentPlayerTurn = p1Id)
        result shouldBe Left(GameError.NotYourTurn)
      }
    }

    "checking if the bidding phase is complete" should {

      "return false if the number of recorded bids is less than the total number of players" in {
        val incompleteBids = BidsCollection.empty + (p1Id -> Bid(1)) + (p2Id -> Bid(0))

        roundManager.isBiddingPhaseComplete(incompleteBids, totalPlayers) shouldBe false
      }

      "return true when every player in the game has submitted a bid" in {
        val completeBids = BidsCollection.empty + (p1Id -> Bid(1)) + (p2Id -> Bid(0)) + (p3Id -> Bid(2))

        roundManager.isBiddingPhaseComplete(completeBids, totalPlayers) shouldBe true
      }
    }

    "calculating the next player's turn" should {

      "advance sequentially to the next player in the list" in {
        val next = roundManager.nextPlayer(current = p1Id, players = mockPlayers)
        next shouldBe Right(p2Id)
      }

      "correctly wrap around to the first player when the last player finishes their turn" in {
        val next = roundManager.nextPlayer(current = p3Id, players = mockPlayers)
        next shouldBe Right(p1Id)
      }
    }
  }