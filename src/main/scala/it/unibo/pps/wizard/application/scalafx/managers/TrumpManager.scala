package it.unibo.pps.wizard.application.scalafx.managers

import it.unibo.pps.wizard.application.scalafx.components.{CardView, TrumpColorSelector}
import it.unibo.pps.wizard.engine.model.basic.{Card, Trump}
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.scene.layout.StackPane

class TrumpManager(val container: StackPane, onColorSelected: Card.Color => Unit):

  private var trumpCardView: Option[CardView] = Option.empty
  private val trumpSelector: TrumpColorSelector = new TrumpColorSelector(onColorSelected)

  def initialize(trump: Trump): Unit =
    container.children.clear()
    enableResolveTrumpColor(false)
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
    container.children.addAll(node, trumpSelector)

  def glowTrump(color: Card.Color): Unit = trumpCardView.foreach(_.setGlow(CardView.fxColor(color)))

  private def createCardView(card: Card): CardView =
    new CardView(card):
      val ch = 240.0
      prefHeight = ch
      prefWidth = ch / 1.2

  def enableResolveTrumpColor(enable: Boolean): Unit =
    trumpSelector.setVisibility(enable)
    trumpCardView.foreach(_.opacity = if enable then 0.4 else 1.0)