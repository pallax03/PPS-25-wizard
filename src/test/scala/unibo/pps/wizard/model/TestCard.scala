package unibo.pps.wizard.model

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class TestCard extends AnyFunSuite with Matchers:
	import Card.*

	test("Color enum contains the four colors"):
		val colors = Color.values.toList
		colors should contain allOf (Color.Blue, Color.Green, Color.Red, Color.Yellow)
		colors.length shouldBe 4

	test("Rank enum values range from 1 to 13 and expose correct numeric values"):
		val rankValues = Rank.values.map(_.value).toList.sorted
		rankValues shouldBe (1 to 13).toList

	test("apply(suit, rank) creates a Standard card with correct color and rank"):
		val c = Card(Color.Blue, Rank.Ten)
		c match
			case Standard(s, r) =>
				s shouldBe Color.Blue
				r shouldBe Rank.Ten
			case other => fail(s"Expected Standard card but got: $other")

	test("wizard and jester factory methods return the special cards"):
		wizard shouldBe Wizard
		jester shouldBe Jester

	test("Standard cards with same color and rank are equal; different ones are not"):
		val a = Card(Color.Green, Rank.One)
		val b = Card(Color.Green, Rank.One)
		val c = Card(Color.Yellow, Rank.One)
		a shouldBe b
		a should not be c
