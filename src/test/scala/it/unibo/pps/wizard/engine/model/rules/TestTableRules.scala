package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.Reasons.{CardNotInHand, MustFollowLeader}
import it.unibo.pps.wizard.engine.model.core.GameError

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.postfixOps

class TestTableRules extends AnyWordSpec with Matchers:
  import Card.*
  import Hand.*
  import Table.*
  import Trump.*
  import TableRules.*

  "TableRules Validation" when:
    val p1 = PlayerId(1)
    val myWizard = wizard
    "a player tries to play a card they don't hold" should:
      "return a CardNotInHand reason" in:
        val hand = 5.blue.asHand
        val result = 10.red.validateAgainst(Table.empty, hand)
        result shouldBe Left(GameError.CardNotAllowed(CardNotInHand))

    "evaluating standard rules" should:
      val c1: Card = 5.blue
      val c2: Card = 10.red
      val hand = (c1 - c2 - myWizard).asHand
      "allow playing anything if there is no leader" in:
        c1.validateAgainst(Table.empty, hand) shouldBe Right(())

      "player HAS to follow the leader color" in:
        val table = Table.empty + (p1 plays 4.blue)
        val result = c2.validateAgainst(table, hand)
        result shouldBe Left(GameError.CardNotAllowed(MustFollowLeader(Blue)))

      "player LACKS the leader color" in:
        val table = Table.empty + (p1 plays 4.yellow)
        c2.validateAgainst(table, hand) shouldBe Right(())

      "always allow special cards even if player has the leader color" in:
        val table = Table.empty + (p1 plays 4.blue)
        myWizard.validateAgainst(table, hand) shouldBe Right(())

  "TableRules Winner Evaluation" should:
    val p1 = PlayerId(1)
    val p2 = PlayerId(2)
    val p3 = PlayerId(3)
    "award the trick to the first Wizard played" in:
      val firstWizard = wizard
      val secondWizard = wizard
      val table = Table.empty + (p1 plays 10.red) + (p2 plays firstWizard) + (p3 plays secondWizard)
      table.evaluateTrickWinner(Trump.Absent) shouldBe p2

    "award the trick to the highest Trump (no Wizard)" in:
      val table = Table.empty + (p1 plays 10.blue) + (p2 plays 2.red) + (p3 plays 5.red)
      table.evaluateTrickWinner(1.red.asTrump) shouldBe p3

    "award the trick to the highest card (no Trump and no Wizard)" in:
      val table = Table.empty + (p1 plays 5.blue) + (p2 plays 10.blue) + (p3 plays 2.yellow)
      table.evaluateTrickWinner(1.green.asTrump) shouldBe p2 // trump color differ from played cards

    "award the trick to the first played Jester if ONLY Jesters are on table" in:
      val table = Table.empty + (p1 plays jester) + (p2 plays jester) + (p3 plays jester)
      table.evaluateTrickWinner(Trump.Absent) shouldBe p1