package it.unibo.pps.wizard.engine.model.basic

opaque type Players = List[Player]

object Players:
  def create(bots: Int): Players = ??? //(0 until bots).map(_ => Player.human(PlayerId(_))).toList
  def create(players: Players, bots: Int): Players = players ++ create(bots)
