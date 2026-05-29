package unibo.pps.wizard.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class TestCard extends AnyFunSuite with Matchers:
	import Card.*

	test("Color enum contains the four colors"):
		val colors = Color.values.toList
		val colors_number = colors.size
		colors should contain allOf (Color.Blue, Color.Green, Color.Red, Color.Yellow)
		colors.length shouldBe colors_number

	test("Rank enum values range from 1 to 13 and expose correct numeric values"):
		val rankValues = Rank.values.map(_.value).toList.sorted
		val min_value = 1
		val max_value = 13
		rankValues shouldBe (min_value to max_value).toList

	test("apply(color, rank) creates a Standard card with correct color and rank"):
		val c = Card(Color.Blue, Rank.Ten)
		c match
			case Standard(s, r) =>
				s shouldBe Color.Blue
				r shouldBe Rank.Ten
			case other => fail(s"Expected Standard card but got: $other")

	test("wizard and jester factory methods return the special cards"):
		val wizard_id = 1
		val jester_id = 2
		wizard(wizard_id) shouldBe Wizard(wizard_id)
		jester(jester_id) shouldBe Jester(jester_id)

	test("Standard cards with same color and rank are equal; different ones are not"):
		val a = Card(Color.Green, Rank.One)
		val b = Card(Color.Green, Rank.One)
		val c = Card(Color.Yellow, Rank.One)
		a shouldBe b
		a should not be c
