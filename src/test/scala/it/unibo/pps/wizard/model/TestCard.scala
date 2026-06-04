package it.unibo.pps.wizard.model

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class TestCard extends AnyWordSpec with Matchers:
	import Card.*

	"Color enum" should:
		"contain the four colors" in:
			val colors = Color.values.toList
			val colors_number = colors.size
			colors should contain allOf (Color.Blue, Color.Green, Color.Red, Color.Yellow)
			colors.length shouldBe colors_number

	"Rank enum values" should:
		"range from 1 to 13 and expose correct numeric values" in:
			val rankValues = Rank.values.map(_.value).toList.sorted
			val min_value = 1
			val max_value = 13
			rankValues shouldBe (min_value to max_value).toList

	"apply(color, rank)" should:
		"create a Standard card with correct color and rank" in:
			val c = Card(Color.Blue, Rank.Ten)
			c match
				case Standard(s, r) =>
					s shouldBe Color.Blue
					r shouldBe Rank.Ten
				case other => fail(s"Expected Standard card but got: $other")

	"wizard and jester factory methods" should:
		"return the special cards" in:
			val wizard_id = 1
			val jester_id = 2
			wizard(wizard_id) shouldBe Wizard(wizard_id)
			jester(jester_id) shouldBe Jester(jester_id)

	"Standard cards equality" should:
		"treat cards with same color and rank as equal and different ones as not equal" in:
			val a = Card(Color.Green, Rank.One)
			val b = Card(Color.Green, Rank.One)
			val c = Card(Color.Yellow, Rank.One)
			a shouldBe b
			a should not be c