package it.unibo.pps.wizard.engine.model.core

import it.unibo.pps.wizard.engine.model.basic.{
  Bid,
  Card,
  Player,
  PlayerId,
  PlayerName,
  Players,
  Round
}
import it.unibo.pps.wizard.engine.model.basic.Card.*
import it.unibo.pps.wizard.engine.model.core.GameError.*
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class TestGameEngine extends AnyWordSpec with Matchers:

  val p0: Player = Player.human(PlayerId(0), PlayerName("Alice"))
  val p1: Player = Player.human(PlayerId(1), PlayerName("Bob"))
  val p2: Player = Player.human(PlayerId(2), PlayerName("Charlie"))
  val p3: Player = Player.human(PlayerId(3), PlayerName("David"))

  val mockPlayers: Players = Players(p0, p1, p2, p3)

  "A GameEngine" should:

    "correctly initialize the game into a Bidding state" in:
      val initialState = GameEngine.initializeGame(mockPlayers)

      initialState shouldBe a[GameState.Bidding]
      val biddingState = initialState.asInstanceOf[GameState.Bidding]

      biddingState.core.players shouldBe mockPlayers
      biddingState.core.round shouldBe Round.start
      biddingState.currentBids shouldBe Map.empty

    "allow the current player to place a valid bid" in:
      val currentBid = Bid(1)
      val initialState = GameEngine.initializeGame(mockPlayers).asInstanceOf[GameState.Bidding]
      val currentPlayer = initialState.currentPlayer
      val action = GameAction.PlaceBid(currentPlayer, currentBid)

      val result = GameEngine.processAction(initialState, action)

      result.isRight shouldBe true
      result.foreach:
        case nextState: GameState.Bidding =>
          nextState.currentBids(currentPlayer) shouldBe currentBid
          nextState.currentPlayer shouldNot be(currentPlayer)
        case _ => fail("Expected GameState.Bidding")

    "fail with NotYourTurn when a player places a bid out of turn" in:
      val initialState = GameEngine.initializeGame(mockPlayers).asInstanceOf[GameState.Bidding]
      val currentPlayer = initialState.currentPlayer
      val wrongPlayer: Player = mockPlayers.toList.find(c => c.id != currentPlayer).get
      val action = GameAction.PlaceBid(wrongPlayer.id, Bid(3))

      val result = GameEngine.processAction(initialState, action)

      result shouldBe Left(NotYourTurn)

    "fail with InvalidAction when executing an action not allowed in Bidding state" in:
      val initialState = GameEngine.initializeGame(mockPlayers).asInstanceOf[GameState.Bidding]
      val playerId = initialState.currentPlayer
      val card: Card = 5.blue
      val action = GameAction.PlayCard(playerId, card)

      val result = GameEngine.processAction(initialState, action)

      result shouldBe Left(InvalidAction)
