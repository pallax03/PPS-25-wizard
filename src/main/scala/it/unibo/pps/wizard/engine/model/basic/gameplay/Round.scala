package it.unibo.pps.wizard.engine.model.basic.gameplay

opaque type Round = Int

object Round:
  def start: Round = 1
  def apply(value: Int): Round = value

  extension (r: Round)
    def value: Int = r
    def next: Round = r + 1
