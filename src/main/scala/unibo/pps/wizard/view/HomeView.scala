package unibo.pps.wizard.view

import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Button, ComboBox, Label, TextField}
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}

class HomeView(onStartGame: (String, Int) => Unit) extends VBox {

  // Configurazione del layout principale della Home
  alignment = Pos.Center
  spacing = 25
  padding = Insets(50)
  style = "-fx-background-color: #1e1e1e;" // Sfondo scuro coerente

  // 1. TITOLO DEL GIOCO
  val titleLabel = new Label("THE WIZARD CARD GAME") {
    font = Font.font("sans-serif", FontWeight.Bold, 42)
    textFill = Color.rgb(230, 126, 34) // Un bel colore arancione acceso
    style = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.6), 10, 0, 0, 4);"
  }

  // 2. CAMPO DI TESTO PER IL NOME
  val nameLabel = new Label("Inserisci il tuo Nome:") {
    style = "-fx-text-fill: #b2bec3; -fx-font-size: 14px;"
  }
  val nameField = new TextField {
    text = "Player 1"
    maxWidth = 250
    style = "-fx-font-size: 14px; -fx-background-color: #2d3436; -fx-text-fill: white; -fx-border-color: #636e72; -fx-border-radius: 5; -fx-background-radius: 5;"
  }

  // 3. MENU A TENDINA PER IL NUMERO DI AVVERSARI
  val opponentsLabel = new Label("Numero di Avversari (Max 5):") {
    style = "-fx-text-fill: #b2bec3; -fx-font-size: 14px;"
  }

  // Creiamo una ComboBox con le opzioni da 1 a 5 avversari
  val opponentsCombo = new ComboBox[Int](Vector(1, 2, 3, 4, 5)) {
    value = 5 // Valore predefinito (per fare il tavolo da 6 totale)
    maxWidth = 100
    style = "-fx-font-size: 14px;"
  }

  // 4. BOTTONE START
  val btnStart = new Button("CREA PARTITA") {
    font = Font.font("sans-serif", FontWeight.Bold, 16)
    style = """
      -fx-background-color: #2ecc71;
      -fx-text-fill: white;
      -fx-padding: 10 30 10 30;
      -fx-background-radius: 20;
      -fx-cursor: hand;
    """

    // Quando viene cliccato, lancia la funzione di callback passando i dati inseriti
    onAction = _ => onStartGame(nameField.text.value, opponentsCombo.value.value)
  }

  // Aggiungiamo tutti gli elementi al VBox della HomeView
  children = Seq(
    titleLabel,
    new VBox(5) { alignment = Pos.Center; children = Seq(nameLabel, nameField) },
    new VBox(5) { alignment = Pos.Center; children = Seq(opponentsLabel, opponentsCombo) },
    btnStart
  )
}