package it.unibo.pps.wizard.application.scalafx.managers

import it.unibo.pps.wizard.application.scalafx.components.CardView
import it.unibo.pps.wizard.engine.model.basic.{Card, Trump}
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

class TrumpManager(val container: VBox):
  
  private var trumpCardView: Option[CardView] = Option.empty
  
  def initialize(trump: Trump): Unit = 
    container.children.clear()
    val node: Node = trump.card match
    case Some(card) => 
      this.trumpCardView = Some(createCardView(card))
      trump.effectiveColor.foreach(glowTrump)
      trumpCardView.head
    case None =>
      new Label("No Trump"):
        style =
          "-fx-text-fill: #555555; -fx-font-weight: bold; -fx-font-size: 12px; -fx-text-alignment: center;"
        minWidth = 80
        minHeight = 80 * 1.2
    container.children.add(node)
  
  def glowTrump(color: Card.Color): Unit = trumpCardView.foreach(_.setGlow(CardView.fxColor(color)))
  
  private def createCardView(card: Card): CardView =
    new CardView(card):
      val ch = 240.0
      prefHeight = ch
      prefWidth = ch / 1.2