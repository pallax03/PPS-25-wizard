package it.unibo.pps.wizard.application.scalafx.components

import it.unibo.pps.wizard.engine.model.basic.cards.Card
import scalafx.geometry.Insets
import scalafx.scene.control.Button
import scalafx.scene.control.Label
import scalafx.scene.effect.DropShadow
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle
import scalafx.scene.text.Font
import scalafx.scene.text.FontWeight

/**
 * A selector for choosing a trump color in the Wizard game.
 *
 * @param onColorSelected a callback function that is called when a color is selected
 */
class TrumpColorSelector(onColorSelected: Card.Color => Unit) extends VBox:
  private val titleLabel = new Label("Select a Color:"):
    style = "-fx-text-fill: white; -fx-text-alignment: center;"
    font = Font.font("Arial", FontWeight.Bold, 16)

  private val buttonsContainer = new VBox:
    margin = Insets(10, 0, 0, 0)
    children = Card.Color.values.map { cardColor =>
      new Button():
        graphic = new Circle {
          radius = 18
          fill = CardView.fxColor(cardColor)
          effect = new DropShadow {
            color = Color.Black; radius = 8; spread = 0.2
          }
        }
        style = "-fx-background-color: transparent; -fx-cursor: hand;"
        onAction = _ =>
          setVisibility(false)
          onColorSelected(cardColor)

        onMouseEntered = _ => {
          scaleX = 1.15
          scaleY = 1.15
        }
        onMouseExited = _ => {
          scaleX = 1.0
          scaleY = 1.0
        }
    }.toSeq

  children = Seq(titleLabel, buttonsContainer)

  def setVisibility(enabled: Boolean): Unit =
    visible = enabled
    managed = enabled
