package it.unibo.pps.wizard.view.components

import it.unibo.pps.wizard.engine.model.basic.{Card, Table}
import scalafx.Includes.jfxMouseDragEvent2sfx
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.control.Label
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.paint.Color


class PlayedCardWrapper(val card: Card, playerName: String, isLeader: Boolean) extends VBox:
  alignment = Pos.Center
  spacing = 8

  val cardView = new CardView(card)

  cardView.prefWidth <== this.width
  cardView.prefHeight <== cardView.prefWidth * 1.4

  val nameLabel = new Label(playerName):
    style = "-fx-text-fill: #e0e0e0; -fx-font-weight: bold; -fx-font-size: 11px;"

  val statusLabel = new Label(if isLeader then "Leader Card" else ""):
    style = "-fx-text-fill: gold; -fx-font-weight: bold; -fx-font-size: 12px;"
    minHeight = 15

  if isLeader then cardView.setGlow(Color.Gold)

  children = Seq(statusLabel, cardView, nameLabel)

class TableView(table: Table) extends HBox:
  alignment = Pos.Center
  padding = Insets(20)
  spacing = 15.0

  private val normalStyle = "-fx-background-color: rgba(43, 92, 63, 0.85); -fx-background-radius: 20;"
  private val hoverStyle = "-fx-background-color: rgba(60, 120, 80, 0.95); -fx-background-radius: 20;"

  style = normalStyle

  val dummyData = table.playedCards.map(c => (c, "Player" + table.playerOf(c), false))
  children = dummyData.map { case (card, playerName, leaderCard: table.leaderCard) =>
    val wrapper = new PlayedCardWrapper(card, playerName, card == leaderCard)
    wrapper.prefWidth <== (this.width - 40 - (5 * spacing.value)) / 6
    wrapper
  }

  def setHighlight(active: Boolean): Unit =
    this.style = if (active) hoverStyle else normalStyle

  def isOver(sceneX: Double, sceneY: Double): Boolean =
    val bounds = this.localToScene(this.boundsInLocal.value)
    bounds.contains(sceneX, sceneY)