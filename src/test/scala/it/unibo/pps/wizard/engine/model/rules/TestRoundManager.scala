package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.{CoreState, GameError, GameState}
import org.scalatest.OptionValues.convertOptionToValuable
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestRoundManager extends AnyWordSpec with Matchers:

  import RoundManager.*
  import Round.*

  "RoundManager" when:
    val players = List(PlayerId(1), PlayerId(2), PlayerId(3)).map(Player.human)

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

    "checking round completion" should:
      "return true if current trick count matches the round number" in:
        Round.start.isComplete(1) shouldBe true
        Round.start.next.isComplete(2) shouldBe true

      "return false if current trick count is less than the round number" in:
        Round.start.isComplete(0) shouldBe false

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

    "checking bidding phase completion" should:
      "return true when the number of bids matches total players" in:
        val bidsCount = 3
        bidsCount.isBiddingPhaseComplete(3) shouldBe true

      "return false when the number of bids is less than total players" in:
        val bidsCount = 1
        bidsCount.isBiddingPhaseComplete(3) shouldBe false

    "initializing a new round" should:
      "correctly transition to Bidding state with distributed hands and trump" in:
        val initialDeck = Deck.create
        val round = Round.start
        val core = CoreState(
          players = Players(players),
          hands = Hands.empty,
          deck = initialDeck,
          round = round,
          dealerId = PlayerId(1),
          scoreboard = Scoreboard.empty
        )

        val (finalCore, biddingState) = round.initialize.run(core).value

        biddingState shouldBe a[GameState.Bidding]
        finalCore.hands.getHand(PlayerId(1)).value.size shouldBe 1
        finalCore.deck.length shouldBe (Deck.TOTAL_SIZE - 4)
        biddingState.currentPlayer shouldBe PlayerId(1)
        biddingState.trump should not be Trump.Absent

      "correctly transition to Bidding state for Round 4" in:
        val initialDeck = Deck.create
        val round3 = Round.start.next.next.next
        val core = CoreState(
          players = Players(players),
          hands = Hands.empty,
          deck = initialDeck,
          round = round3,
          dealerId = PlayerId(3),
          scoreboard = Scoreboard.empty
        )

        val (finalCore, biddingState) = round3.initialize.run(core).value

        biddingState shouldBe a[GameState.Bidding]
        finalCore.hands.getHand(PlayerId(1)).value.size shouldBe 4
        finalCore.deck.length shouldBe (Deck.TOTAL_SIZE - 13)

        biddingState.currentPlayer shouldBe PlayerId(1)
        biddingState.trump should not be Trump.Absent
