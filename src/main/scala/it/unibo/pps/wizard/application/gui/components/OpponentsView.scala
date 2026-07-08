package it.unibo.pps.wizard.application.gui.components

import it.unibo.pps.wizard.engine.model.basic.{Bid, PlayerId, Players}
import scalafx.scene.Node
import scalafx.scene.layout.HBox

class OpponentsView(val container: HBox):

  private var opponents: Map[PlayerId, Node] = Map.empty

  def renderAllOpponents(players: Players, currentPlayerId: PlayerId): Unit =
    container.children.clear()
    opponents = Map.empty
    players.toList.foreach: player =>
      val opponentView = BotPlayerView(player, player.id == currentPlayerId)
      container.children.add(opponentView)
      opponents += (player.id -> opponentView)

  def updateOpponentBid(playerId: PlayerId, bid: Bid): Unit =
    opponents.get(playerId) match
      case Some(opponentView: BotPlayerView) =>
        opponentView.updateBid(bid)
      case _ => println(s"Opponent with ID $playerId not found.")
