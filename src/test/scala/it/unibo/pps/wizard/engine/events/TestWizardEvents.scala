package it.unibo.pps.wizard.engine.events

import it.unibo.pps.wizard.engine.events.ActionEvent.*
import it.unibo.pps.wizard.engine.events.InvitationEvent.*
import it.unibo.pps.wizard.engine.events.ProgressEvent.*
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.basic.Card.*
import it.unibo.pps.wizard.engine.model.basic.Table.*
import it.unibo.pps.wizard.engine.model.core.{CoreState, GameAction, GameState}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestWizardEvents extends AnyWordSpec with Matchers:

  private val player1 = PlayerId(1)
  private val name1 = PlayerName("Alice")
  private val name2 = PlayerName("Bob")
  private val player2 = PlayerId(2)
  
  private val players = Players(Player.human(player1, name1), Player.human(player2, name2))
  private val core = CoreState(
    players = players,
    hands = Hands.empty,
    deck = Deck.create,
    round = Round.start,
    dealerId = player1,
    scoreboard = Scoreboard.empty
  )

  "ActionEvent" should:
    "map a player action to the matching event" in:
      val card = 7.red

      ActionEvent.from(GameAction.PlayCard(player1, card)) shouldBe CardPlayed(player1, card)

  "ProgressEvent" should:
    "compose the events produced by the last trick of a round" in:
      val table = Table.empty + (player1 plays 7.red) + (player2 plays 8.red)
      val oldState = GameState.Playing(core, Trump.Absent, Bids.empty, table, player2, Tricks.empty)
      val newState = GameState.Bidding(core, Trump.Absent, Bids.empty, player1)

      ProgressEvent.fromTransition(oldState, newState) shouldBe List(
        TrickWon(player1, table.playedCards),
        RoundScored(core.scoreboard),
        CardsDealt(core.hands, Trump.Absent),
        PhaseChanged(newState)
      )

  "InvitationEvent" should:
    "ask the bidding player for a bid" in:
      val state = GameState.Bidding(core, Trump.Absent, Bids.empty, player1)

      InvitationEvent.fromState(state) shouldBe Some(WaitingForBid(player1))

    "not ask for actions after the game has ended" in:
      InvitationEvent.fromState(GameState.Ended(Scoreboard.empty)) shouldBe None
