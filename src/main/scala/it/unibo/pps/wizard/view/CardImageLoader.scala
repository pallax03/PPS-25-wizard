package it.unibo.pps.wizard.view

import it.unibo.pps.wizard.engine.model.basic.Card
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

      case Card.Wizard(id) => id match
        case 0 => "/cards/blue/BW.webp"
        case 1 => "/cards/green/GW.webp"
        case 2 => "/cards/red/RW.webp"
        case 3 => "/cards/yellow/YW.webp"

      case Card.Jester(id) => id match
        case 0 => "/cards/blue/BJ.webp"
        case 1 => "/cards/green/GJ.webp"
        case 2 => "/cards/red/RJ.webp"
        case 3 => "/cards/red/YJ.webp"
    }

    // Carica la risorsa dalla cartella src/main/resources
    val resource = getClass.getResource(path)
    if (resource == null) {
      throw new IllegalArgumentException(s"Immagine non trovata al path: $path")
    }
    new Image(resource.toExternalForm)
  }
}