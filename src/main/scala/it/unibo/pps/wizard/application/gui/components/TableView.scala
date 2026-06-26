package it.unibo.pps.wizard.application.gui.components

import it.unibo.pps.wizard.engine.model.basic.{Card, Table}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.control.Label
import scalafx.geometry.{Insets, Pos}

// todo: refactoring of this helper
class PlayedCardWrapper(val card: Card, playerName: String, isLeader: Boolean) extends VBox:
  alignment = Pos.Center
  spacing = 8

  val cardView = new CardView(card)

  cardView.prefWidth <== this.width
  cardView.prefHeight <== cardView.prefWidth * 1.4

  val nameLabel: Label = new Label(playerName):
    style = "-fx-text-fill: #e0e0e0; -fx-font-weight: bold; -fx-font-size: 11px;"

  private val statusLabel = new Label(if isLeader then "Following Card" else ""):
    style = "-fx-text-fill: gold; -fx-font-weight: bold; -fx-font-size: 12px;"
    minHeight = 15

  if isLeader then card match
    case Card.Standard(color, rank) => cardView.setGlow(CardView.fxColor(color))
    case _ =>

  children = Seq(statusLabel, cardView, nameLabel)

class TableView(table: Table) extends HBox:
  alignment = Pos.Center
  padding = Insets(20)
  spacing = 15.0

  private val normalStyle = "-fx-background-color: rgba(43, 92, 63, 0.85); -fx-background-radius: 15;"
  private val hoverStyle = "-fx-background-color: rgba(60, 120, 80, 0.95); -fx-background-radius: 15;"

  style = normalStyle

  val check: (c: Card) => Boolean = (c) => table.followingCard match
    case Some(value) if c == value => true
    case _ => false
  private val dummyData = table.playedCards.map(c => (c, "Player " + table.playerOf(c).get, check(c)))
  children = dummyData.map { case (card, playerName, isLeader) =>
    val wrapper = new PlayedCardWrapper(card, playerName, isLeader)
    wrapper.prefWidth <== (this.width - 40 - (5 * spacing.value)) / 6
    wrapper
  }

  def setHighlight(active: Boolean): Unit =
    this.style = if (active) hoverStyle else normalStyle

  def isOver(sceneX: Double, sceneY: Double): Boolean =
    val bounds = this.localToScene(this.boundsInLocal.value)
    bounds.contains(sceneX, sceneY)