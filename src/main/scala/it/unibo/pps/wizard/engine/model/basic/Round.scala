package it.unibo.pps.wizard.engine.model.basic

opaque type Round = Int

object Round:
  def start: Round = 0
  extension (round: Round)
    def value: Int = round
    def increment: Round = round + 1
