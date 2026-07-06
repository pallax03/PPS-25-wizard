package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.basic.Card.*
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

  def createMockCore(roundValue: Int): CoreState =
    CoreState(
      players = mockPlayers,
      hands = Hands.empty,
      deck = Deck.create,
      round = Round(roundValue),
      dealerId = PlayerId(0),
      scoreboard = Scoreboard.empty
    )

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
      val currentBids: Bids = Bids.empty + (PlayerId(1) -> Bid(0)) + (PlayerId(2) -> Bid(1)) + (PlayerId(3) -> Bid(0))

      val biddingState = GameState.Bidding(
        core = core,
        trump = Trump.Absent,
        currentBids = currentBids,
        currentPlayer = PlayerId(4)
      )

      val action = GameAction.PlaceBid(PlayerId(4), Bid(1))
      val result = GameEngine.processAction(biddingState, action)

      result.isRight shouldBe true
      result.foreach:
        case playingState: GameState.Playing =>
          playingState.bids.size shouldBe 4
          playingState.bids(PlayerId(4)).value shouldBe Bid(1).value
          playingState.table.isEmpty shouldBe true
        case _ => fail("Expected GameState.Playing")

    "allow resolving an unresolved wizard trump during bidding phase" in:
      val core = createMockCore(2)
      val wizardUnresolved = Trump.WizardUnresolved(Card.wizard)
      val biddingState = GameState.Bidding(
        core = core,
        trump = wizardUnresolved,
        currentBids = Bids.empty,
        currentPlayer = PlayerId(1)
      )

      val action = GameAction.ChooseTrump(PlayerId(1), Red)
      val result = GameEngine.processAction(biddingState, action)

      result.isRight shouldBe true
      result.foreach:
        case nextState: GameState.Bidding =>
          nextState.trump.effectiveColor shouldBe Some(Red)
        case _ => fail("Expected GameState.Bidding with resolved Trump")

    "fail with CardNotAllowed when a player tries to play a card they do not own" in:
      val c1 = 5 of Blue
      val c2 = 10 of Red

      val hands = Hands(Map(PlayerId(1) -> Hand(List(c1)), PlayerId(2) -> Hand(List(c2))))
      val core = createMockCore(1).copy(hands = hands)

      val playingState = GameState.Playing(
        core = core,
        trump = Trump.Absent,
        bids = Bids.empty,
        table = Table.empty,
        currentPlayerTurn = PlayerId(1),
        tricksWon = Tricks.empty
      )

      val action = GameAction.PlayCard(PlayerId(1), c2)
      val result = GameEngine.processAction(playingState, action)

      result shouldBe Left(CardNotAllowed(CardNotInHand))

    "allow playing a card, removing it from hand and adding it to the table" in:
      val c1 = 5 of Blue
      val hands = Hands(Map(PlayerId(1) -> Hand(List(c1))))
      val core = createMockCore(1).copy(hands = hands)

      val playingState = GameState.Playing(
        core = core,
        trump = Trump.Absent,
        bids = Bids.empty,
        table = Table.empty,
        currentPlayerTurn = PlayerId(1),
        tricksWon = Tricks.empty
      )

      val action = GameAction.PlayCard(PlayerId(1), c1)
      val result = GameEngine.processAction(playingState, action)

      result.isRight shouldBe true
      result.foreach:
        case nextState: GameState.Playing =>
          nextState.table.playedCards should contain (c1)
          nextState.currentPlayerTurn shouldBe PlayerId(2)
        case _ => fail("Expected GameState.Playing")

    "evaluate the trick winner and reset the table when the trick is complete" in:
      val c0 = 2 of Blue
      val c1 = 10 of Blue
      val c2 = 4 of Red
      val c3 = 5 of Blue

      val extraCard = 3 of Yellow

      val hands = Hands(Map(
        PlayerId(1) -> Hand(List(c0, extraCard)),
        PlayerId(2) -> Hand(List(c1, extraCard)),
        PlayerId(3) -> Hand(List(c2, extraCard)),
        PlayerId(4) -> Hand(List(c3, extraCard))
      ))
      val core = createMockCore(2).copy(hands = hands)

      val currentTable = Table.empty + (PlayerId(1) -> c0) + (PlayerId(2) -> c1) + (PlayerId(3) -> c2)

      val playingState = GameState.Playing(
        core = core,
        trump = Trump.Absent,
        bids = Bids.empty,
        table = currentTable,
        currentPlayerTurn = PlayerId(4),
        tricksWon = Tricks.initialize(mockPlayers.toList)
      )

      val action = GameAction.PlayCard(PlayerId(4), c3)
      val result = GameEngine.processAction(playingState, action)

      result.isRight shouldBe true
      result.foreach:
        case nextState: GameState.Playing =>
          nextState.table.isEmpty shouldBe true
          nextState.tricksWon(PlayerId(2)) shouldBe 1
          nextState.currentPlayerTurn shouldBe PlayerId(2)
        case _ => fail("Expected GameState.Playing")