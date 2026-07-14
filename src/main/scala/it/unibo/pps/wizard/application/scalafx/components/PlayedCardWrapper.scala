package it.unibo.pps.wizard.application.scalafx.components

import it.unibo.pps.wizard.engine.model.basic.Card
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

class PlayedCardWrapper(
    val card: Card,
    playerName: String,
    isWinning: Boolean,
    isFollowing: Boolean
) extends VBox:
  alignment = Pos.Center
  spacing = 8

  val cardView = new CardView(card)

  cardView.prefWidth <== this.width
  cardView.prefHeight <== cardView.prefWidth * 1.4

  val nameLabel: Label = new Label(playerName):
    style = "-fx-text-fill: #e0e0e0; -fx-font-weight: bold; -fx-font-size: 11px;"

  private val followingLabel = new Label(if isFollowing then "Following Card" else ""):
    style =
      "-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 12px;" // todo: matching css color using CardView.fxcolor(color)
    minHeight = 15

  private val winningLabel = new Label(if isWinning then "Winning Card" else ""):
    style = "-fx-text-fill: gold; -fx-font-weight: bold; -fx-font-size: 12px;"
    minHeight = 15

//  if isTrump then
//    card match
//      case Card.Standard(color, rank) => cardView.setGlow(CardView.fxColor(color))
//      case _                          =>

  children = Seq(followingLabel, winningLabel, cardView, nameLabel)
