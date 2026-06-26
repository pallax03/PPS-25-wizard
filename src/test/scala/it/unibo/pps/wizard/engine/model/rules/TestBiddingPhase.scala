package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameError
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestBiddingPhase extends AnyWordSpec with Matchers:
  val biddingRules: BiddingRules = BiddingRules()

  val p1Id: PlayerId = PlayerId(1)
  val p2Id: PlayerId = PlayerId(2)
  val p3Id: PlayerId = PlayerId(3)

  val totalPlayers = 3
  val currentRound: Round = Round.start.next.next

  "The BiddingRules" when {

    "processing a player's bid" should {

      "reject it with InvalidBid if the bid value is negative" in {
        val result = biddingRules.processBid(
          bid = Bid(-1),
          currentBids = BidsCollection.empty,
          currentPlayer = p1Id,
          round = currentRound,
          totalPlayers = totalPlayers
        )
        result shouldBe Left(GameError.InvalidBid)
      }

      "reject it with InvalidBid if the bid exceeds the maximum allowed tricks for the current Round" in {
        val result = biddingRules.processBid(
          bid = Bid(3),
          currentBids = BidsCollection.empty,
          currentPlayer = p1Id,
          round = currentRound,
          totalPlayers = totalPlayers
        )
        result shouldBe Left(GameError.InvalidBid)
      }

      "accept it and record it accurately within the BidsCollection if valid" in {
        val result = biddingRules.processBid(
          bid = Bid(1),
          currentBids = BidsCollection.empty,
          currentPlayer = p1Id,
          round = currentRound,
          totalPlayers = totalPlayers
        )

        result.isRight shouldBe true
        val updatedBids = result.toOption.get
        updatedBids.size shouldBe 1
        updatedBids.getBid(p1Id) shouldBe Some(Bid(1))
      }

      "reject it with InvalidBid if it is the last player and the total sum would equal the round number (Hook Rule)" in {
        val bidsAfterP1 = biddingRules.processBid(Bid(1), BidsCollection.empty, p1Id, currentRound, totalPlayers).toOption.get

        val bidsAfterP2 = biddingRules.processBid(Bid(0), bidsAfterP1, p2Id, currentRound, totalPlayers).toOption.get

        val illegalResult = biddingRules.processBid(
          bid = Bid(1),
          currentBids = bidsAfterP2,
          currentPlayer = p3Id,
          round = currentRound,
          totalPlayers = totalPlayers
        )

        illegalResult shouldBe Left(GameError.InvalidBid)
      }

      "accept the final player's bid if it complies with the Hook Rule" in {
        val bidsAfterP1 = biddingRules.processBid(Bid(1), BidsCollection.empty, p1Id, currentRound, totalPlayers).toOption.get

        val bidsAfterP2 = biddingRules.processBid(Bid(0), bidsAfterP1, p2Id, currentRound, totalPlayers).toOption.get

        val validResult = biddingRules.processBid(
          bid = Bid(2),
          currentBids = bidsAfterP2,
          currentPlayer = p3Id,
          round = currentRound,
          totalPlayers = totalPlayers
        )

        validResult.isRight shouldBe true
        val finalBids = validResult.toOption.get
        finalBids.getBid(p3Id) shouldBe Some(Bid(2))
        finalBids.sum shouldBe Bid(3)
      }
    }
  }