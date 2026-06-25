package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameError
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestTableRules extends AnyWordSpec with Matchers:

  val rules = TableRules()
  val p1: PlayerId = PlayerId(1)
  val p2: PlayerId = PlayerId(2)
  val p3: PlayerId = PlayerId(3)
  val p4: PlayerId = PlayerId(4)

  val red2: Card = Card(Color.Red, Rank.Two)
  val red5: Card = Card(Color.Red, Rank.Five)
  val red10: Card = Card(Color.Red, Rank.Ten)
  val red13: Card = Card(Color.Red, Rank.Thirteen)

  val blue10: Card = Card(Color.Blue, Rank.Ten)
  val blue13: Card = Card(Color.Blue, Rank.Thirteen)
  val blueTrump: Trump = Trump.Standard(Card.Standard(Color.Blue, Rank.One))

  val green5: Card = Card(Color.Green, Rank.Five)
  val green10: Card = Card(Color.Green, Rank.Ten)

  val yellow2: Card = Card(Color.Yellow, Rank.Two)
  val yellowTrump: Trump = Trump.WizardResolved(Card.wizard(4), Color.Yellow)

  val wiz1: Card = Card.wizard(1)
  val wiz2: Card = Card.wizard(2)
  val jest1: Card = Card.jester(1)
  val jest2: Card = Card.jester(2)
  val jest3: Card = Card.jester(3)
  val jest4: Card = Card.jester(4)

  "TableRules" when:

    "validating a Card play" should:
      val emptyTable: Table = Table.empty
      val tableRedLeader: Table = emptyTable.addCard(p1, red5)
      val handWithLeader: Hand = Hand(red10, blue10)
      val handWithoutLeader: Hand = Hand(green5, blue10)
      val handWithSpecials: Hand = Hand(red2, wiz1, jest1)
      val handMissingCard: Hand = Hand(red10, blue10)

      def check(table: Table, hand: Hand, card: Card) = rules.validateCard(table, hand, card, Trump.Absent)
      "allow playing any card if the table is empty" in:
        check(emptyTable, handWithLeader, red10) shouldBe Right(())

      "block the play if the card is not physically in the hand" in:
        check(emptyTable, handMissingCard, wiz1) shouldBe Left(GameError.CardNotAllowed)

      "enforce the Leader Color rule (must follow suit if possible)" in:
        check(tableRedLeader, handWithLeader, blue10) shouldBe Left(GameError.CardNotAllowed)

      "allow discarding if the player lacks the Leader Color" in:
        check(tableRedLeader, handWithoutLeader, blue10) shouldBe Right(())

      "always allow playing a Special Card (Wizard/Jester) ignoring suits" in:
        check(tableRedLeader, handWithSpecials, wiz1) shouldBe Right(())
        check(tableRedLeader, handWithSpecials, jest1) shouldBe Right(())


    "evaluating the Trick Winner" should:
      def makeTable(plays: (PlayerId, Card)*): Table =
        plays.foldLeft(Table.empty)((t, play) => t.addCard(play._1, play._2))
      "prioritize the FIRST Wizard over everything else (Trumps and Leaders)" in:
        val table = makeTable(p1 -> red10, p2 -> wiz1, p3 -> wiz2, p4 -> blue13)
        rules.evaluateTrickWinner(table, blueTrump) shouldBe p2

      "prioritize the highest Trump if no Wizards are played" in:
        val table = makeTable(p1 -> red10, p2 -> red13, p3 -> yellow2, p4 -> jest1)
        rules.evaluateTrickWinner(table, yellowTrump) shouldBe p3

      "prioritize the highest Leader Color if no Wizards and no Trumps are played" in:
        val table = makeTable(p1 -> green5, p2 -> red13, p3 -> green10, p4 -> jest2)
        rules.evaluateTrickWinner(table, blueTrump) shouldBe p3

      "prioritize the FIRST player if everyone plays a Jester" in:
        val table = makeTable(p1 -> jest1, p2 -> jest2, p3 -> jest3, p4 -> jest4)
        rules.evaluateTrickWinner(table, Trump.Absent) shouldBe p1