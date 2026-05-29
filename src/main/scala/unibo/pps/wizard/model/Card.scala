package unibo.pps.wizard.model

/**
 * A card in the Wizard game.
 *
 * Implemented as a sealed trait with three concrete kinds:
 *  - Standard: a color + rank card (1..13)
 *  - Wizard: the special Wizard card
 *  - Jester: the special Jester card
 *
 * Companion object provides enums for Color and Rank
 * and factory methods.
 */
sealed trait Card

sealed trait SpecialCard extends Card:
  def id: Int

object Card:
  enum Color:
    case Blue, Green, Red, Yellow

  /**
   * Rank in the standard color cards. Values 1..13 (1 is low, 13 is high).
   */
  enum Rank(val value: Int):
    case One    extends Rank(1)
    case Two    extends Rank(2)
    case Three  extends Rank(3)
    case Four   extends Rank(4)
    case Five   extends Rank(5)
    case Six    extends Rank(6)
    case Seven  extends Rank(7)
    case Eight  extends Rank(8)
    case Nine   extends Rank(9)
    case Ten    extends Rank(10)
    case Eleven extends Rank(11)
    case Twelve extends Rank(12)
    case Thirteen extends Rank(13)

  final case class Standard(color: Color, rank: Rank) extends Card
  final case class Wizard(id: Int) extends SpecialCard
  final case class Jester(id: Int) extends SpecialCard

  def apply(color: Color, rank: Rank): Card = Standard(color, rank)
  def wizard(id: Int): Card = Wizard(id)
  def jester(id: Int): Card = Jester(id)
