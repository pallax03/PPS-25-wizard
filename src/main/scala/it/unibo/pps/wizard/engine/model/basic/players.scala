package it.unibo.pps.wizard.engine.model.basic

opaque type PlayerId = Int
object PlayerId:
  def apply(s: Int): PlayerId = s

/**
 * Represent a player in the game that can be human or computer.
 */
final case class Player(id: PlayerId, isBot: Boolean)

object Player:
  def human(id: PlayerId): Player = Player(id, isBot = false)
  def computer(id: PlayerId): Player = Player(id, isBot = true)

opaque type Players = List[Player]

object Players:
  def apply(players: List[Player]): Players = players

  private def generateComputers(numberOfComputers: Int): Players = (1 to numberOfComputers).map(id => Player.computer(PlayerId(id))).toList
  def create(players: Players, numberOfComputers: Int): Players = players ++ generateComputers(numberOfComputers)

  extension (players: Players)
    def toList: List[Player] = players.toList


