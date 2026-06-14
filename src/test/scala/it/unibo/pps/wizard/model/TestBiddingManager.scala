package it.unibo.pps.wizard.model

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TestBiddingManager extends AnyWordSpec with Matchers {
  // --- TEST FIXTURES (MOCKS BASED ON YOUR MODEL) ---
  val p1Id: PlayerId = 1
  val p2Id: PlayerId = 2
  val p3Id: PlayerId = 3

  // Building proper Player entities with the required Hand structure
  val player1: Player = Player.human(p1Id, "Alice").receiveCards(List(Card(Card.Color.Blue, Card.Rank.One)))
  val player2: Player = Player.human(p2Id, "Bob").receiveCards(List(Card(Card.Color.Red, Card.Rank.Five)))
  val player3: Player = Player.human(p3Id, "Charlie").receiveCards(List(Card(Card.Color.Green, Card.Rank.Ten)))
  val mockPlayers: List[Player] = List(player1, player2, player3)

  // Generating a real shuffled deck using your factory
  val mockDeck: Deck = Deck()
  val mockTrump: Option[Trump] = None

  // Setup: Round 2, Dealer is Player 1 (p1Id).
  // According to Wizard rules, the first to bid is the player after the dealer -> Player 2 (p2Id)
  val initialCore = CoreState(
    players = mockPlayers,
    deck = mockDeck,
    round = Round(2),
    dealerId = p1Id
  )

  val initialBiddingState = GameState.Bidding(
    core = initialCore,
    trump = mockTrump,
    currentBids = BidsCollection.empty,
    currentPlayer = p2Id
  )

  // --- ANYWORDSPEC SPECIFICATIONS ---

  "The BiddingManager" when {

    "receiving a bid from a player" should {

      "reject it with NotYourTurn if it is placed out of order" in {
        // Player 1 tries to bid, but it's currently Player 2's turn
        val action = GameAction.PlaceBid(p1Id, Bid(1))
        val result = BiddingManager.processBid(initialBiddingState, action)

        result shouldBe Left(GameError.NotYourTurn)
      }

      "reject it with InvalidBid if the bid value is negative" in {
        // Negative bids are structurally invalid in Wizard
        val action = GameAction.PlaceBid(p2Id, Bid(-1))
        val result = BiddingManager.processBid(initialBiddingState, action)

        result shouldBe Left(GameError.InvalidBid)
      }

      "reject it with InvalidBid if the bid exceeds the maximum allowed tricks for the current Round" in {
        // We are in round 2, so bidding 3 tricks is illegal
        val action = GameAction.PlaceBid(p2Id, Bid(3))
        val result = BiddingManager.processBid(initialBiddingState, action)

        result shouldBe Left(GameError.InvalidBid)
      }

      "accept it, record it accurately, and advance the turn sequentially if the bid is valid" in {
        val action = GameAction.PlaceBid(p2Id, Bid(1))
        val result = BiddingManager.processBid(initialBiddingState, action)

        result.isRight shouldBe true
        val nextState = result.toOption.get

        // Verify the game state remains in Bidding and the turn passed to Player 3
        nextState shouldBe a[GameState.Bidding]
        val biddingState = nextState.asInstanceOf[GameState.Bidding]

        biddingState.currentPlayer shouldBe p3Id
        biddingState.currentBids.getBid(p2Id) shouldBe Some(Bid(1))
      }

      "reject it with InvalidBid if it is the last player and the total sum would equal the round number" in {
        val stateAfterP2 = BiddingManager.processBid(initialBiddingState, GameAction.PlaceBid(p2Id, Bid(1)))
          .toOption.get.asInstanceOf[GameState.Bidding]

        val stateAfterP3 = BiddingManager.processBid(stateAfterP2, GameAction.PlaceBid(p3Id, Bid(0)))
          .toOption.get.asInstanceOf[GameState.Bidding]

        val illegalAction = GameAction.PlaceBid(p1Id, Bid(1))
        val result = BiddingManager.processBid(stateAfterP3, illegalAction)

        result shouldBe Left(GameError.InvalidBid)
      }
    }

    "all players in the game have placed their bids" should {

      "automatically transition to the Playing state and initialize all round properties" in {
        // 1. Player 2 bids 1 trick
        val stateAfterP2 = BiddingManager.processBid(initialBiddingState, GameAction.PlaceBid(p2Id, Bid(1)))
          .toOption.get.asInstanceOf[GameState.Bidding]

        // 2. Player 3 bids 0 tricks
        val stateAfterP3 = BiddingManager.processBid(stateAfterP2, GameAction.PlaceBid(p3Id, Bid(0)))
          .toOption.get.asInstanceOf[GameState.Bidding]

        // 3. Last player (the Dealer, Player 1) places the final bid of 2 tricks
        val finalAction = GameAction.PlaceBid(p1Id, Bid(2))
        val finalResult = BiddingManager.processBid(stateAfterP3, finalAction)

        finalResult.isRight shouldBe true
        val nextState = finalResult.toOption.get

        // --- PLAYING STATE VERIFICATIONS ---
        nextState shouldBe a[GameState.Playing]
        val playingState = nextState.asInstanceOf[GameState.Playing]

        // A. Verify that all bids are saved and retrievable via BidsCollection
        playingState.bids.getBid(p2Id) shouldBe Some(Bid(1))
        playingState.bids.getBid(p3Id) shouldBe Some(Bid(0))
        playingState.bids.getBid(p1Id) shouldBe Some(Bid(2))

        // B. Verify that the table is cleared and completely empty
        playingState.table shouldBe Table.empty

        // C. Verify that the lead turn (currentPlayerTurn & leadPlayer) goes to the player after the dealer (p2)
        playingState.currentPlayerTurn shouldBe p2Id
        playingState.leadPlayer shouldBe p2Id

        // D. Verify that the tricksWon tracking system is freshly initialized to 0 for every player
        playingState.tricksWon.getTricks(p1Id) shouldBe 0
        playingState.tricksWon.getTricks(p2Id) shouldBe 0
        playingState.tricksWon.getTricks(p3Id) shouldBe 0
      }
    }
  }
}