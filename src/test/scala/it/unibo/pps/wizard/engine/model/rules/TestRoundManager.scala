package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameError
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestRoundManager extends AnyWordSpec with Matchers:

  import RoundManager.*
  import Round.*

  val p0: Player = Player.human(PlayerId(1), PlayerName("Alice"))
  val p1: Player = Player.human(PlayerId(2), PlayerName("Bob"))
  val p2: Player = Player.human(PlayerId(3), PlayerName("Charlie"))

  val players: Players = Players(p0, p1, p2)

  "RoundManager" when:

    "managing turn order" should:
      "find the next player correctly" in:
        players.nextAfter(PlayerId(1)) shouldBe Right(PlayerId(2))
        players.nextAfter(PlayerId(3)) shouldBe Right(PlayerId(1))

      "fail if current player is not in the list" in:
        players.nextAfter(PlayerId(99)) shouldBe Left(GameError.NotYourTurn)

    "determining the first player of a round" should:
      "rotate correctly based on the round number" in:
        val round = Round.start
        round.firstPlayer(players) shouldBe PlayerId(1)
        round.next.firstPlayer(players) shouldBe PlayerId(2)
        round.next.next.firstPlayer(players) shouldBe PlayerId(3)
        round.next.next.next.firstPlayer(players) shouldBe PlayerId(1)

    "dealing cards" should:
      "distribute the correct amount of cards based on the round" in:
        val initialDeck = Deck.create
        val round = Round.start
        val (deckAfter, (hands, trump)) = round.deal(players).run(initialDeck).value

        hands.getHand(PlayerId(1)).size shouldBe 1
        hands.getHand(PlayerId(2)).size shouldBe 1
        trump shouldBe defined
        deckAfter.length shouldBe (Deck.TOTAL_SIZE - 3 - 1)

      "handle deals where no cards are left for the trump card" in:
        val initialDeck = Deck.create
        val maxRound = (1 until 20).foldLeft(Round.start)((r, _) => r.next)

        val (deckAfter, (hands, trump)) = maxRound.deal(players).run(initialDeck).value

        hands.getHand(PlayerId(1)).value.size shouldBe 20
        trump shouldBe empty
        deckAfter.length shouldBe 0

    "validating the turn of a player" should:
      "succeed if the action player matches the expected player" in:
        val expected = PlayerId(2)
        expected.validateTurnOf(PlayerId(2)) shouldBe Right(())

      "fail with NotYourTurn if the action player is different" in:
        val expected = PlayerId(2)
        expected.validateTurnOf(PlayerId(1)) shouldBe Left(GameError.NotYourTurn)

//    "initializing a new round" should:
//      "correctly transition to Bidding state if trump is not Unresolved" in:
//        val initialDeck = Deck.create
//        val round = Round.start
//        val core = CoreState(
//          players = players,
//          hands = Hands.empty,
//          deck = initialDeck,
//          trump = Trump.Absent,
//          round = round,
//          dealerId = PlayerId(1),
//          scoreboard = Scoreboard.empty
//        )
//
//        val (finalCore, initialState) = round.initialize.run(core).value
//
//        biddingState shouldBe a[GameState.Bidding]
//        finalCore.hands.getHand(PlayerId(1)).value.size shouldBe 1
//        finalCore.deck.length shouldBe (Deck.TOTAL_SIZE - 4)
//        biddingState.currentPlayer shouldBe PlayerId(1)
//        biddingState.trump should not be Trump.Absent
//
//      "correctly transition to Bidding state for Round 4" in:
//        val initialDeck = Deck.create
//        val round3 = Round.start.next.next.next
//        val core = CoreState(
//          players = players,
//          hands = Hands.empty,
//          deck = initialDeck,
//          round = round3,
//          dealerId = PlayerId(3),
//          scoreboard = Scoreboard.empty
//        )
//
//        val (finalCore, biddingState) = round3.initialize.run(core).value
//
//        biddingState shouldBe a[GameState.Bidding]
//        finalCore.hands.getHand(PlayerId(1)).value.size shouldBe 4
//        finalCore.deck.length shouldBe (Deck.TOTAL_SIZE - 13)
//
//        biddingState.currentPlayer shouldBe PlayerId(1)
//        biddingState.trump should not be Trump.Absent
