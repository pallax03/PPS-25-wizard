package it.unibo.pps.wizard.engine.model.basic

import it.unibo.pps.wizard.engine.model.basic.Card
import it.unibo.pps.wizard.engine.model.basic.Card.Wizard
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.postfixOps

class TestCard extends AnyWordSpec with Matchers:
	import Card.*
	"A card" should:
		"from 1 to 13" in:
			an [NoSuchElementException] shouldBe thrownBy (0 of Red)
			an [NoSuchElementException] shouldBe thrownBy (14 of Red)
		"Create a Standard" in:
			13 of Red shouldBe a [Standard]
		"Create a Wizard" in:
			wizard shouldBe a [Wizard]
		"Create a Jester" in:
			jester shouldBe a [Jester]
		"guarantee that special cards are unique instances" in:
				wizard should not equal wizard
	"Some Cards" should:
		"create a chain of Cards" in:
			val myCards: List[Card] = 5.red - 4.yellow - wizard - 10.green - jester - 13.blue
			myCards should have size 6
			myCards(0) shouldBe (5 of Red)
			myCards(1) shouldBe (4 of Yellow)
			myCards(2) shouldBe a [Wizard]
			myCards(3) shouldBe (10 of Green)
			myCards(4) shouldBe a [Jester]
			myCards(5) shouldBe (13 of Blue)