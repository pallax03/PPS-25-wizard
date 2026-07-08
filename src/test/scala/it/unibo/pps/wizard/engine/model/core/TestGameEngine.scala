package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.basic.Card.*
import it.unibo.pps.wizard.engine.model.rules.RoundManager.*
import it.unibo.pps.wizard.engine.model.core.GameError.*
import it.unibo.pps.wizard.engine.model.core.Reasons.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TestGameEngine extends AnyWordSpec with Matchers:

  val p1: Player = Player.human(PlayerId(1), PlayerName("Alice"))
  val p2: Player = Player.human(PlayerId(2), PlayerName("Bob"))
  val p3: Player = Player.human(PlayerId(3), PlayerName("Charlie"))
  val p4: Player = Player.human(PlayerId(4), PlayerName("David"))

  val mockPlayers: Players = Players(p1, p2, p3, p4)

  def createMockCore(roundValue: Int): CoreState = {
    val round = Round(roundValue)
    CoreState(
      players = mockPlayers,
      hands = Hands.empty,
      deck = Deck.create,
      round = round,
      trump = Trump.Absent,
      dealerId = round.firstPlayer(mockPlayers),
      scoreboard = Scoreboard.empty
    )
  }

  "A GameEngine" should:

    "correctly initialize the game into a Bidding state" in:
      val initialState = GameEngine.initializeGame(mockPlayers)

      initialState shouldBe a[GameState.Bidding]
      val biddingState = initialState.asInstanceOf[GameState.Bidding]

      biddingState.core.players shouldBe mockPlayers
      biddingState.core.round.value shouldBe 1
      biddingState.currentBids.size shouldBe 0

    "allow the current player to place a valid bid" in:
      val currentBid = Bid(1)
      val initialState = GameEngine.initializeGame(mockPlayers).asInstanceOf[GameState.Bidding]
      val currentPlayer = initialState.currentPlayer
      val action = GameAction.PlaceBid(currentPlayer, currentBid)

      val result = GameEngine.processAction(initialState, action)

      result.isRight shouldBe true
      result.foreach:
        case nextState: GameState.Bidding =>
          nextState.currentBids(currentPlayer).value shouldBe currentBid.value
          nextState.currentPlayer shouldNot be(currentPlayer)
        case _ => fail("Expected GameState.Bidding")

    "fail with NotYourTurn when a player places a bid out of turn" in:
      val initialState = GameEngine.initializeGame(mockPlayers).asInstanceOf[GameState.Bidding]
      val currentPlayer = initialState.currentPlayer
      val wrongPlayer: Player = mockPlayers.toList.find(c => c.id != currentPlayer).get
      val action = GameAction.PlaceBid(wrongPlayer.id, Bid(3))

      val result = GameEngine.processAction(initialState, action)

      result shouldBe Left(NotYourTurn)

    "transition from Bidding to Playing phase when the last player places their bid" in:
      val core = createMockCore(1)
      val currentBids: Bids =
        Bids.empty + (p1.id -> Bid(0)) + (p2.id -> Bid(1)) + (p3.id -> Bid(0))

      val biddingState = GameState.Bidding(
        core = core,
        currentBids = currentBids,
        currentPlayer = p4.id
      )

      val action = GameAction.PlaceBid(p4.id, Bid(1))
      val result = GameEngine.processAction(biddingState, action)

      result.isRight shouldBe true
      result.foreach:
        case playingState: GameState.Playing =>
          playingState.bids.size shouldBe 4
          playingState.bids(p4.id).value shouldBe Bid(1).value
          playingState.table.isEmpty shouldBe true
        case _ => fail("Expected GameState.Playing")

    "allow resolving an unresolved wizard trump during ChoosingTrump phase" in:
      val core = createMockCore(2)
      val wizardUnresolved = Trump.WizardUnresolved(Card.wizard)

      val choosingState = GameState.ChoosingTrump(
        core = core.updateTrump(wizardUnresolved)
      )

      val action = GameAction.ResolveTrumpColor(p2.id, Red)
      val result = GameEngine.processAction(choosingState, action)

      result.isRight shouldBe true
      result.foreach:
        case nextState: GameState.Bidding =>
          nextState.core.trump.effectiveColor shouldBe Some(Red)
        case _ => fail("Expected GameState.Bidding with resolved Trump")

    "fail with CardNotAllowed when a player tries to play a card they do not own" in:
      val c1 = 5 of Blue
      val c2 = 10 of Red

      val hands = Hands(Map(p1.id -> Hand(List(c1)), p2.id -> Hand(List(c2))))
      val core = createMockCore(1).copy(hands = hands)

      val playingState = GameState.Playing(
        core = core,
        bids = Bids.empty,
        table = Table.empty,
        currentPlayerTurn = p1.id,
        tricksWon = Tricks.empty
      )

      val action = GameAction.PlayCard(p1.id, c2)
      val result = GameEngine.processAction(playingState, action)

      result shouldBe Left(CardNotAllowed(CardNotInHand))

    "allow playing a card, removing it from hand and adding it to the table" in:
      val c1 = 5 of Blue
      val hands = Hands(Map(p1.id -> Hand(List(c1))))
      val core = createMockCore(1).copy(hands = hands)

      val playingState = GameState.Playing(
        core = core,
        bids = Bids.empty,
        table = Table.empty,
        currentPlayerTurn = p1.id,
        tricksWon = Tricks.empty
      )

      val action = GameAction.PlayCard(p1.id, c1)
      val result = GameEngine.processAction(playingState, action)

      result.isRight shouldBe true
      result.foreach:
        case nextState: GameState.Playing =>
          nextState.table.playedCards should contain(c1)
          nextState.currentPlayerTurn shouldBe p2.id
        case _ => fail("Expected GameState.Playing")

    "evaluate the trick winner and reset the table when the trick is complete" in:
      val c0 = 2 of Blue
      val c1 = 10 of Blue
      val c2 = 4 of Red
      val c3 = 5 of Blue

      val extraCard = 3 of Yellow

      val hands = Hands(
        Map(
          p1.id -> Hand(List(c0, extraCard)),
          p2.id -> Hand(List(c1, extraCard)),
          p3.id -> Hand(List(c2, extraCard)),
          p4.id -> Hand(List(c3, extraCard))
        )
      )
      val core = createMockCore(2).copy(hands = hands)

      val currentTable =
        Table.empty + (p1.id -> c0) + (p2.id -> c1) + (p3.id -> c2)

      val playingState = GameState.Playing(
        core = core,
        bids = Bids.empty,
        table = currentTable,
        currentPlayerTurn = p4.id,
        tricksWon = Tricks.initialize(mockPlayers.toList)
      )

      val action = GameAction.PlayCard(p4.id, c3)
      val result = GameEngine.processAction(playingState, action)

      result.isRight shouldBe true
      result.foreach:
        case nextState: GameState.Playing =>
          nextState.table.isEmpty shouldBe true
          nextState.tricksWon(p2.id) shouldBe 1
          nextState.currentPlayerTurn shouldBe p2.id
        case _ => fail("Expected GameState.Playing")
