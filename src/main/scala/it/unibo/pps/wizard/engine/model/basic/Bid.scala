package it.unibo.pps.wizard.engine.model.basic

opaque type Bid = Int

object Bid:
  def apply(value: Int): Bid = value

  val zero: Bid = apply(0)

  extension (b: Bid)
    def toInt: Int = b
    def +(other: Bid): Bid = b + other
    def >=(other: Bid): Boolean = (b: Int) >= (other: Int)
    def <=(round: Round): Boolean = b <= round.toInt