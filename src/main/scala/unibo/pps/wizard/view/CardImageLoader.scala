package unibo.pps.wizard.view

import unibo.pps.wizard.model.Card
import scalafx.scene.image.Image

object CardImageLoader {

  /**
   * Restituisce un oggetto Image di ScalaFX caricato dalla cartella resources.
   */
  def getImageForCard(card: Card): Image = {
    val path = card match {
      case Card.Standard(color, rank) =>
        val colorDir = color.toString.toLowerCase // "blue", "red", etc.
        val colorInit = color.toString.substring(0, 1).toUpperCase // "B", "R", etc.
        s"/cards/$colorDir/$colorInit${rank.value}.webp"

      case Card.Wizard =>
        "/cards/yellow/YW.webp" // Assumi questa posizione per le speciali

      case Card.Jester =>
        "/cards/red/RJ.webp"
    }

    // Carica la risorsa dalla cartella src/main/resources
    val resource = getClass.getResource(path)
    if (resource == null) {
      throw new IllegalArgumentException(s"Immagine non trovata al path: $path")
    }
    new Image(resource.toExternalForm)
  }
}