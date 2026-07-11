package it.unibo.pps.wizard.engine.prolog

import it.unibo.pps.wizard.engine.model.basic.Card.*
import it.unibo.pps.wizard.engine.model.basic.Hand.*
import it.unibo.pps.wizard.engine.model.basic.{Table, Bid}
import it.unibo.pps.wizard.engine.model.rules.TableRules.validateAgainst

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestWizardPrologEngine extends AnyWordSpec with Matchers:

  "WizardPrologEngine" when:
    val engine = WizardPrologEngine()
    val hand = (1.red - jester - wizard - 12.yellow).asHand
    "choosing trump color" in:
      val trumpColor = engine.chooseTrumpColor(hand).head
      Color.values should contain(trumpColor)

    "place bid" in:
      val bid = engine.placeBid(hand, Option(1.yellow).asTrump).head
      bid.value should be >= 0
      bid.value should be <= hand.size

    "adjust bid" in:
      val bid = engine.adjustBid(hand, Bid(hand.size + 1)).head
      bid.value should be >= 0
      bid.value should be <= hand.size

    "best playable card" in:
      val legalCards = hand.toList.filter(_.validateAgainst(Table.empty, hand).isRight)
      val bestCard = engine
        .bestPlayableCard(
          hand = hand,
          winningCard = 10.yellow,
          followingColor = Yellow,
          trump = Option(5.blue).asTrump,
          playerBid = Bid(5),
          playerTrick = Bid(3)
        )
        .head
      legalCards should contain(bestCard)
