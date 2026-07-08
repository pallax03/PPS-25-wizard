package it.unibo.pps.wizard.application.gui.components

import it.unibo.pps.wizard.engine.model.basic.Card
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox


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

  if isLeader then
    card match
      case Card.Standard(color, rank) => cardView.setGlow(CardView.fxColor(color))
      case _                          =>

  children = Seq(statusLabel, cardView, nameLabel)