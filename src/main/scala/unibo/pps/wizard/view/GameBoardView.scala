package unibo.pps.wizard.view

import scalafx.animation.ScaleTransition
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.Label
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{BorderPane, HBox, StackPane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.util.Duration
import unibo.pps.wizard.model.Card

class GameBoardView(humanName: String, opponentCount: Int, hand: List[Card]) extends BorderPane {

  // Configurazione del BorderPane principale del tavolo
  padding = Insets(20)
  style = "-fx-background-color: #1c1c18;"

  // --- Mappatura Colori Modello -> Colori Grafici (JavaFX) ---
  private def mapColor(modelColor: Card.Color): Color = modelColor match {
    case Card.Color.Red => Color.LightCoral
    case Card.Color.Blue => Color.LightSkyBlue
    case Card.Color.Green => Color.LightGreen
    case Card.Color.Yellow => Color.Khaki
  }

  // --- Nuovo metodo per creare la carta partendo dal Modello ---
  private def createCardNode(card: Card): StackPane = {
    try {
      val cardImage = CardImageLoader.getImageForCard(card)

      // Creiamo lo StackPane che fa da contenitore alla carta
      val cardContainer = new StackPane {
        children = Seq(
          new ImageView(cardImage) {
            fitWidth = 120
            fitHeight = 170
            preserveRatio = true
            smooth = true
          }
        )
      }

      // --- CONFIGURAZIONE ANIMAZIONE DI HOVER ---

      // Creiamo la transizione di scala sul contenitore della carta
      val scaleAnimation = new ScaleTransition {
        duration = Duration(150) // Durata dell'effetto in millisecondi (veloce e reattivo)
        node = cardContainer     // Il bersaglio dell'animazione
      }

      // Quando il mouse ENTRA nell'area della carta
      cardContainer.onMouseEntered = _ => {
        scaleAnimation.stop() // Ferma eventuali animazioni in corso per evitare scatti
        scaleAnimation.toX = 1.15  // Ingrandisce del 15% in larghezza
        scaleAnimation.toY = 1.15  // Ingrandisce del 15% in altezza
        scaleAnimation.play()
      }

      // Quando il mouse ESCE dall'area della carta
      cardContainer.onMouseExited = _ => {
        scaleAnimation.stop()
        scaleAnimation.toX = 1.0   // Ritorna alla dimensione originale
        scaleAnimation.toY = 1.0
        scaleAnimation.play()
      }

      cardContainer // Ritorna il nodo configurato con le sue animazioni

    } catch {
      case e: Exception =>
        println(s"Errore caricamento carta: ${e.getMessage}")
        new StackPane {
          children = Seq(
            new Rectangle { width = 70; height = 105; fill = Color.Red },
            new Label("Err") { style = "-fx-text-fill: white;" }
          )
        }
    }
  }

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
        children = hand.map(card => createCardNode(card))
      }
    )
  }
}