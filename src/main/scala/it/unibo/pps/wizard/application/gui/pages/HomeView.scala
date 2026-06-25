package it.unibo.pps.wizard.application.gui.pages

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, ComboBox, Label, TextField}
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}

class HomeView(onStartGame: () => Unit) extends VBox {

  alignment = Pos.Center
  spacing = 25
  padding = Insets(50)
  style = "-fx-background-color: #1e1e1e;"

  private val titleLabel = new Label("THE WIZARD CARD GAME") {
    font = Font.font("sans-serif", FontWeight.Bold, 42)
    textFill = Color.rgb(230, 126, 34)
    style = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10, 0, 0, 4);"
  }

  val nameLabel: Label = new Label("Inserisci il tuo Nome:") {
    style = "-fx-text-fill: #b2bec3; -fx-font-size: 14px;"
  }
  private val nameField = new TextField {
    text = "Player 1"
    maxWidth = 250
    style = "-fx-font-size: 14px; -fx-background-color: #2d3436; -fx-text-fill: white; -fx-border-color: #636e72; -fx-border-radius: 5; -fx-background-radius: 5;"
  }

  private val opponentsLabel = new Label("Numero di Avversari (Max 5):") {
    style = "-fx-text-fill: #b2bec3; -fx-font-size: 14px;"
  }

  private val opponentsCombo = new ComboBox[Int](Vector(1, 2, 3, 4, 5)) {
    value = 5
    maxWidth = 100
    style = "-fx-font-size: 14px;"
  }

  private val btnStart = new Button("CREA PARTITA") {
    font = Font.font("sans-serif", FontWeight.Bold, 16)
    style = """
      -fx-background-color: #2ecc71;
      -fx-text-fill: white;
      -fx-padding: 10 30 10 30;
      -fx-background-radius: 20;
      -fx-cursor: hand;
    """

    onAction = _ => onStartGame()
    // TODO: insert here if need other parameters
  }

  children = Seq(
    titleLabel,
    new VBox(5) { alignment = Pos.Center; children = Seq(nameLabel, nameField) },
    new VBox(5) { alignment = Pos.Center; children = Seq(opponentsLabel, opponentsCombo) },
    btnStart
  )
}