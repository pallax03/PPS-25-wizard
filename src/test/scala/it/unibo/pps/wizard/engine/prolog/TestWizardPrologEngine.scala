package it.unibo.pps.wizard.engine.prolog

import it.unibo.pps.wizard.engine.model.basic.Card.*
import it.unibo.pps.wizard.engine.model.basic.Hand.*
import it.unibo.pps.wizard.engine.model.basic.{Table, Bid, Trick}
import it.unibo.pps.wizard.engine.model.rules.TableRules.*

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestWizardPrologEngine extends AnyWordSpec with Matchers:

  "WizardPrologEngine" should:
    val engine = WizardPrologEngine()
    val hand = ((One of Red) - jester - wizard - (Twelve of Yellow)).asHand
    "choosing trump color" in:
      val trumpColor = engine.chooseTrumpColor(hand).head
      Color.values should contain(trumpColor)

    "place bid" in:
      val bid = engine.placeBid(hand, Option(One of Yellow).asTrump).head
      bid should be >= 0
      bid should be <= hand.size

    "adjust bid" in:
      val bid = engine.adjustBid(hand, Bid(hand.size + 1)).head
      bid should be >= 0
      bid should be <= hand.size

    "best playable card" in:
      val legalCards = hand.legalCards(Table.empty)
      val bestCard = engine
        .bestPlayableCard(
          hand = hand,
          winningCard = Option(Ten of Yellow),
          followingColor = Option(Yellow),
          trump = Option(Five of Blue).asTrump,
          playerBid = Bid(5),
          playerTrick = Trick(3)
        )
        .head
      legalCards should contain(bestCard)
