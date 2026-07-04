package it.unibo.pps.wizard.engine.model.basic

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestBids extends AnyWordSpec with Matchers:
  import Bid.*
  import Bids.*

  val p1: PlayerId = PlayerId(1)
  val p2: PlayerId = PlayerId(2)

  "A Bid" when:
    "created" should:
      "expose the correct integer value" in:
        Bid(5).value shouldBe 5
        Bid.zero.value shouldBe 0

    "compared" should:
      "support comparison operations" in:
        (Bid(5) >= Bid(3)) shouldBe true
        (Bid(2) >= Bid(2)) shouldBe true
        (Bid(1) >= Bid(5)) shouldBe false

    "validated" should:
      "be valid if within round range" in:
        val round = Round.start.next // Round 2
        Bid(2).isValid(round) shouldBe true
        Bid(3).isValid(round) shouldBe false

  "Bids collection" when:
    "empty" should:
      val bids = Bids.empty
      "return zero for any player" in:
        bids(p1) shouldBe Bid.zero

      "have size zero and total zero" in:
        bids.isComplete(3) shouldBe false
        bids.total shouldBe Bid.zero

    "receiving bids" should:
      val bids = Bids.empty + (p1 -> Bid(1)) + (p2 -> Bid(2))
      "store and retrieve bids correctly" in:
        bids(p1) shouldBe Bid(1)
        bids(p2) shouldBe Bid(2)

      "calculate the correct total" in:
        bids.total shouldBe Bid(3)

      "identify when complete" in:
        bids.isComplete(2) shouldBe true
        bids.isComplete(3) shouldBe false
