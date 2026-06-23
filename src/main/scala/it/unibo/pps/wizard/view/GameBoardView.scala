package it.unibo.pps.wizard.view

import it.unibo.pps.wizard.engine.model.basic.{Card, Hand, Trump}
import scalafx.Includes.jfxMouseEvent2sfx
import scalafx.animation.{ParallelTransition, ScaleTransition, TranslateTransition}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{BorderPane, HBox, StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.util.Duration

class GameBoardView(humanName: String, opponentCount: Int, hand: Hand, trump: Trump) extends BorderPane {

  // Configurazione del BorderPane principale del tavolo
  padding = Insets(20)
  style = "-fx-background-color: #1c1c18;"

  private def createOpponentSlot(name: String): VBox = new VBox {
    alignment = Pos.Center
    spacing = 5
    children = Seq(
      new Rectangle { width = 70; height = 50; fill = Color.rgb(60, 63, 65); stroke = Color.LightGray; arcWidth = 10; arcHeight = 10 },
      new Label(name) { style = "-fx-text-fill: white; -fx-font-size: 11px;" }
    )
  }

  // --- Composizione del Layout ---

  // 1. Avversari in alto
  private val opponents = (1 to opponentCount).map(i => createOpponentSlot(s"Computer $i"))
  top = new HBox(40) {
    alignment = Pos.Center
    padding = Insets(10)
    children = opponents
  }

  // 2. Panno verde centrale
  center = new StackPane {
    style = "-fx-background-color: #2b5c3f; -fx-background-radius: 30; -fx-border-color: #1a3a26; -fx-border-radius: 30; -fx-border-width: 3;"
    minWidth = 400
    minHeight = 200
    children = new Label(s"Tavolo di Gioco - Avversari: $opponentCount") {
      style = "-fx-text-fill: #a5d6a7; -fx-font-size: 14px; -fx-font-style: italic;"
    }
  }

  // 3. La tua mano in basso
  bottom = new VBox(10) {
    alignment = Pos.Center
    padding = Insets(15)
    style = "-fx-background-color: rgba(255, 255, 255, 0.05); -fx-background-radius: 15;"
    children = Seq(
      new Label(s"MANO DI: ${humanName.toUpperCase}") {
        style = "-fx-text-fill: #e0e0e0; -fx-font-weight: bold; -fx-font-size: 12px;"
      },
      new HBox(10) {
        alignment = Pos.Center
        children = HandView(hand)
      }
    )
  }
}