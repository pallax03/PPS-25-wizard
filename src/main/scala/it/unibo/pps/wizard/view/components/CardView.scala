package it.unibo.pps.wizard.view.components

import it.unibo.pps.wizard.engine.model.basic.Card
import scalafx.geometry.Pos
import scalafx.scene.effect.DropShadow
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.layout.StackPane
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle
import scalafx.scene.text.{Font, FontWeight, Text}

class CardView(val card: Card) extends StackPane:
  alignment = Pos.Center

  minWidth = 0
  minHeight = 0

  private val imagePath = getImagePath(card)
  private val cardImage = Option(getClass.getResourceAsStream(imagePath)).map(stream => new Image(stream))

  cardImage match
    case Some(img) =>
      val imageView = new ImageView(img):
        preserveRatio = true
        smooth = true

      imageView.fitWidth <== this.prefWidth
      imageView.fitHeight <== this.prefHeight

      children = imageView

    case None =>
      val bgRect = new Rectangle:
        fill = getFallbackColor(card)
        arcWidth = 15
        arcHeight = 15
        stroke = Color.Black
        strokeWidth = 2

      bgRect.width <== this.width
      bgRect.height <== this.height

      val valueText = new Text(getFallbackText(card)):
        font = Font.font("Arial", FontWeight.Bold, 24)
        fill = Color.White
        style = "-fx-effect: dropshadow(gaussian, black, 3, 1.0, 0, 0);"

      children = Seq(bgRect, valueText)

  def setGlow(glowColor: Color): Unit =
    this.effect = new DropShadow:
      this.color = glowColor
      radius = 20
      spread = 0.5

  def removeGlow(): Unit =
    this.effect = null

  private def getImagePath(c: Card): String = c match
    case Card.Standard(color, rank) => s"/cards/${color.toString.toLowerCase}/${color.toString.substring(0, 1).toUpperCase}${rank.value}.webp"
    case Card.Wizard(id) => specialCardsImages(getFallbackText(c), id)
    case Card.Jester(id) => specialCardsImages(getFallbackText(c), id)

  private def specialCardsImages(name: String, id: Int): String = id match
    case 0 => s"/cards/yellow/Y$name.webp"
    case 1 => s"/cards/blue/B$name.webp"
    case 2 => s"/cards/red/R$name.webp"
    case 3 => s"/cards/green/G$name.webp"

  private def getFallbackColor(c: Card): Color = c match
    case Card.Standard(color, _) => color match
      case Card.Color.Red => Color.FireBrick
      case Card.Color.Blue => Color.DodgerBlue
      case Card.Color.Green => Color.ForestGreen
      case Card.Color.Yellow => Color.Goldenrod
    case Card.Wizard(_) => Color.Purple
    case Card.Jester(_) => Color.Orange

  private def getFallbackText(c: Card): String = c match
    case Card.Standard(_, rank) => rank.value.toString
    case Card.Wizard(_) => "W"
    case Card.Jester(_) => "J"