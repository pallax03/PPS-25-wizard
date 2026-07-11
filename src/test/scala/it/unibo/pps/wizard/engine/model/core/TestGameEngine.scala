package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.events.{ActionEvent, ProgressEvent}
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.basic.Card.*
import it.unibo.pps.wizard.engine.model.basic.Hand.*
import it.unibo.pps.wizard.engine.model.core.GameError.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TestGameEngine extends AnyWordSpec with Matchers:

  val p1: Player = Player.human(PlayerId(1), PlayerName("Alice"))
  val p2: Player = Player.human(PlayerId(2), PlayerName("Bob"))
  val p3: Player = Player.human(PlayerId(3), PlayerName("Charlie"))
  val p4: Player = Player.human(PlayerId(4), PlayerName("David"))

  val mockPlayers: Players = Players(p1, p2, p3, p4)

  def createMockCore(roundValue: Int): CoreState =
    val round = Round(roundValue)
    CoreState.initialize(mockPlayers, round)

  "A GameEngine" should:
    "allow resolving an unresolved wizard trump during ChoosingTrump phase" in:
      val core = createMockCore(1).updateTrump(Option(wizard).asTrump)
      val choosingState = GameState.ChoosingTrump(core)
      val action = GameAction.ResolveTrumpColor(p1.id, Color.Red)

      val result = GameEngine.processAction(choosingState, action)

      result.isRight shouldBe true
      result.foreach: engine =>
        engine.state match
          case nextState: GameState.Bidding =>
            nextState.core.trump.effectiveColor shouldBe Some(Color.Red)
          case _ => fail("Expected GameState.Bidding")
        engine.events should contain(ActionEvent.TrumpColorResolved(p1.id, Color.Red))

    "allow the current player to place a valid bid" in:
      val core = createMockCore(1)
      val biddingState = GameState.Bidding(core, Bids.empty, p1.id)
      val action = GameAction.PlaceBid(p1.id, Bid(1))

      val result = GameEngine.processAction(biddingState, action)

      result.isRight shouldBe true
      result.foreach: engine =>
        engine.state match
          case nextState: GameState.Bidding =>
            nextState.currentBids(p1.id).value shouldBe 1
            nextState.currentPlayer shouldBe p2.id
          case _ => fail("Expected GameState.Bidding")

        engine.events should contain(ActionEvent.BidPlaced(p1.id, Bid(1)))

    "fail with NotYourTurn when a player places a bid out of turn" in:
      val core = createMockCore(1)
      val biddingState = GameState.Bidding(core, Bids.empty, p1.id)
      val action = GameAction.PlaceBid(p2.id, Bid(3))

      val result = GameEngine.processAction(biddingState, action)

      result shouldBe Left(NotYourTurn)

    "transition from Bidding to Playing phase when the last player places their bid" in:
      val hands = Hands.empty + (p1.id -> 5.red.asHand)
      val core = createMockCore(1).copy(hands = hands)

      val currentBids = Bids.empty + (p1.id -> Bid(0)) + (p2.id -> Bid(1)) + (p3.id -> Bid(0))
      val biddingState = GameState.Bidding(core, currentBids, p4.id)

      val action = GameAction.PlaceBid(p4.id, Bid(1))
      val result = GameEngine.processAction(biddingState, action)

      result.isRight shouldBe true
      result.foreach: engine =>
        engine.state match
          case playingState: GameState.Playing =>
            playingState.bids.size shouldBe 4
            playingState.table.isEmpty shouldBe true
            playingState.currentPlayerTurn shouldBe p1.id
          case _ => fail("Expected GameState.Playing")

        engine.events.exists(_.isInstanceOf[ProgressEvent.PhaseChanged]) shouldBe true

    "allow playing a card, removing it from hand and adding it to the table" in:
      val c1 = 5.blue
      val hands = Hands.empty + (p1.id -> c1.asHand) + (p2.id -> c1.asHand)
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
      result.foreach: engine =>
        engine.state match
          case nextState: GameState.Playing =>
            nextState.table.playedCards should contain(c1)
            nextState.currentPlayerTurn shouldBe p2.id
          case _ => fail("Expected GameState.Playing")

    "evaluate the trick winner and reset the table when the trick is complete" in:
      val c0 = 2.blue
      val c1 = 10.blue
      val c2 = 4.red
      val c3 = 5.blue

      val extraCard = 3.yellow
      val hands = Hands.empty
        + (p1.id -> extraCard.asHand)
        + (p2.id -> extraCard.asHand)
        + (p3.id -> extraCard.asHand)
        + (p4.id -> (c3 - extraCard).asHand)

      val core = createMockCore(2).copy(hands = hands)
      val currentTable = Table.empty + (p1.id -> c0) + (p2.id -> c1) + (p3.id -> c2)

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
      result.foreach: engine =>
        engine.state match
          case nextState: GameState.Playing =>
            nextState.table.isEmpty shouldBe true
            nextState.tricksWon(p2.id) shouldBe 1
            nextState.currentPlayerTurn shouldBe p2.id
          case _ => fail("Expected GameState.Playing")