package it.unibo.pps.wizard.engine.model.basic

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.postfixOps

class TestTrump extends AnyWordSpec with Matchers:
  import Trump.*
  import Card.*
  "A Trump" when:
    "Absent" should:
      val trump = Trump.Absent
      "provide no card and no effective color" in:
        trump.card shouldBe None
        trump.effectiveColor shouldBe None

    "created from a Standard card" should:
      val trump = 5.red.asTrump
      "extract color and card natively" in:
        trump shouldBe a [Trump.Standard]
        trump.effectiveColor shouldBe Some(Red)
        trump.card.get shouldBe (5 of Red)

    "created from a Jester" should:
      val j = jester
      val trump = j.asTrump
      "behave like Absent but remember the physical card" in:
        trump shouldBe a [Trump.Jester]
        trump.effectiveColor shouldBe None
        trump.card shouldBe Some(j)

    "created from a Wizard" should:
      val w = wizard
      val trump = w.asTrump
      "initialized as Unresolved" in:
        trump shouldBe a [Trump.WizardUnresolved]
        trump.effectiveColor shouldBe None
        trump.card shouldBe Some(w)

      "Unresolved to Resolved" in:
        val resolvedTrump = trump.asInstanceOf[Trump.WizardUnresolved] resolvedAs Blue
        resolvedTrump shouldBe a [Trump.WizardResolved]
        resolvedTrump.effectiveColor shouldBe Some(Blue)
        resolvedTrump.card shouldBe Some(w)