package it.unibo.pps.wizard.view.components

import it.unibo.pps.wizard.engine.model.basic.Trump
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.layout.HBox

class TrumpView(trump: Trump) extends HBox:
  alignment = Pos.Center
  padding = Insets(0)
  spacing = 0

//  style = "-fx-background-color: #A0A2A180; -fx-background-radius: 15;"
  trump.getCard match
    case Some(card) =>
      val cardView = new CardView(card)
      val ch = 240.0
      cardView.prefHeight = ch
      cardView.prefWidth = ch / 1.2

      trump.effectiveColor.foreach(color =>
        cardView.setGlow(CardView.fxColor(color))
      )

      children = cardView
    case None =>
      val placeholder = new Label("No Trump"):
        style = "-fx-text-fill: #555555; -fx-font-weight: bold; -fx-font-size: 12px; -fx-text-alignment: center;"
        minWidth = 80
        minHeight = 80 * 1.2
        alignment = Pos.Center

      children = placeholder
