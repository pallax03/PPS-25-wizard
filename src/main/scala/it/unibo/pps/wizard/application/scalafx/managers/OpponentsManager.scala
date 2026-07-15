package it.unibo.pps.wizard.application.scalafx.managers

import it.unibo.pps.wizard.application.scalafx.components.{BasePlayerView, BotPlayerView}
import it.unibo.pps.wizard.engine.model.basic.{Bid, PlayerId, Players}
import scalafx.scene.Node
import scalafx.scene.layout.HBox

class OpponentsManager(val container: HBox):

  private var opponents: Map[PlayerId, Node] = Map.empty

  def renderAllOpponents(players: Players): Unit =
    container.children.clear()
    opponents = Map.empty
    players.toList.foreach: player =>
      val opponentView = BotPlayerView(player)
      container.children.add(opponentView)
      opponents += (player.id -> opponentView)

  def updateOpponentBid(playerId: PlayerId, bid: Bid): Unit =
    opponents.get(playerId) match
      case Some(opponentView: BotPlayerView) =>
        opponentView.updateBid(bid.toString)
      case _ => println(s"Opponent with ID $playerId not found.")

  def updateActiveTurn(currentTurnPlayerId: PlayerId, phase: String): Unit =
    opponents.foreach: (id, view) =>
      view match
        case bot: BasePlayerView => bot.setTurnActive(id == currentTurnPlayerId, phase)
        case _                   =>

  def resetOpponentsBid(): Unit =
    opponents.values.foreach:
      case bot: BotPlayerView =>
        bot.resetBid()
      case _                  =>

  def updateOpponentsTricksWon(winnerId: PlayerId, tricks: Int): Unit =
    opponents.get(winnerId) match
      case Some(opponentView: BotPlayerView) =>
        opponentView.updateTricksWon(tricks)
      case _ =>
