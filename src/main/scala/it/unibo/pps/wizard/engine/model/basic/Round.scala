package it.unibo.pps.wizard.engine.model.basic

opaque type Round = Int

object Round:
  def apply(value: Int): Round = value

  extension (r: Round)
    def toInt: Int = r
    def next: Round = Round(r + 1)