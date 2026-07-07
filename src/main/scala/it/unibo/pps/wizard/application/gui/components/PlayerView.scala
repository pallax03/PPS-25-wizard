package it.unibo.pps.wizard.application.gui.components

import it.unibo.pps.wizard.engine.model.basic.{Bid, Player}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle
import scalafx.scene.text.{Font, FontWeight}

class PlayerView(val player: Player, val isCurrentTurn: Boolean = false) extends VBox:
  alignment = Pos.Center
  spacing = 4
  padding = Insets(8, 15, 8, 15)

  private val normalStyle =
    "-fx-background-color: rgba(45, 52, 54, 0.7); -fx-background-radius: 10; -fx-border-color: #636e72; -fx-border-width: 1; -fx-border-radius: 10;"
  private val activeTurnStyle =
    "-fx-background-color: rgba(230, 126, 34, 0.2); -fx-background-radius: 10; -fx-border-color: #e67e22; -fx-border-width: 2; -fx-border-radius: 10;"

  style = if isCurrentTurn then activeTurnStyle else normalStyle

  private val avatarIndicator = new Circle:
    radius = 12
    fill = if isCurrentTurn then Color.rgb(230, 126, 34) else Color.rgb(178, 190, 195)

  private val nameLabel = new Label(player.name.toString):
    font = Font.font("Arial", FontWeight.Bold, 12)
    textFill = Color.White

  private val roleLabel = new Label(if player.isBot then "Bot" else "Tu"):
    font = Font.font("Arial", FontWeight.Normal, 10)
    textFill = if player.isBot then Color.rgb(140, 140, 140) else Color.rgb(230, 126, 34)
    
  private val bidLabel = new Label(s"Bid: 0"):
    font = Font.font("Arial", FontWeight.Normal, 10)
    textFill = Color.rgb(178, 190, 195)

  def updateBid(bid: Bid): Unit =
    bidLabel.text = s"Bid: $bid"

  children = Seq(avatarIndicator, nameLabel, roleLabel, bidLabel)
