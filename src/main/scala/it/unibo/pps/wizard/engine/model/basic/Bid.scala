package it.unibo.pps.wizard.engine.model.basic

opaque type Bid = Int

object Bid:
  def apply(value: Int): Bid = value

  extension (b: Bid)
    def inc: Bid = b+1
    def value: Int = b
