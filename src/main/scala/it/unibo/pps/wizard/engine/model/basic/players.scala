package it.unibo.pps.wizard.engine.model.basic

opaque type PlayerId = Int
object PlayerId:
  def apply(s: Int): PlayerId = s
  
opaque type PlayerName = String
object PlayerName:
  def apply(s: String): PlayerName = s

/**
 * Represent a player in the game that can be human or computer.
 */
final case class Player(id: PlayerId, name: PlayerName, isBot: Boolean)

object Player:
  def human(id: PlayerId, name: PlayerName): Player = Player(id, name, isBot = false)
  def computer(id: PlayerId): Player = Player(id, PlayerName(s"Computer $id"), isBot = true)

opaque type Players = List[Player]

object Players:
  def apply(players: Player*): Players = players.toList
  def create(players: Players, numberOfComputers: Int): Players = players ++ generateComputers(numberOfComputers)
  private def generateComputers(numberOfComputers: Int): Players = (1 to numberOfComputers).map(id => Player.computer(PlayerId(id))).toList

  extension (players: Players)
    def toList: List[Player] = players.toList
