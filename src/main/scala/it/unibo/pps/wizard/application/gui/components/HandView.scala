package it.unibo.pps.wizard.application.gui.components

import it.unibo.pps.wizard.engine.model.basic.{Card, Hand}

import scalafx.Includes.jfxMouseEvent2sfx
import scalafx.animation.{ParallelTransition, ScaleTransition, TranslateTransition}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.layout.FlowPane
import scalafx.util.Duration

class HandView(
    hand: Hand,
    onCardDragged: (Double, Double) => Unit,
    onCardDropped: (Card, Double, Double) => Unit
) extends FlowPane:
  alignment = Pos.Center
  style = "-fx-background-color: #A0A2A180; -fx-background-radius: 15;"
  padding = Insets(10)
  maxHeight = 180

  val cards: List[Card] = hand.toList
  private val cardCount: Int = math.max(1, cards.size)
  private val overlapRatio: Double = if (cardCount > 10) -0.03 else -0.01

  hgap <== this.width * overlapRatio
  vgap = 5.0

  private val targetCardHeight = 130.0
  private val targetCardWidth = targetCardHeight / 1.2

  children = cards.map: card =>
    val cardView = new CardView(card)

    cardView.prefWidth = targetCardWidth
    cardView.prefHeight = targetCardHeight

    var dragContextX = 0.0
    var dragContextY = 0.0

    cardView.onMousePressed = event =>
      dragContextX = event.sceneX - cardView.translateX.value
      dragContextY = event.sceneY - cardView.translateY.value

      cardView.delegate.setViewOrder(-10.0)

      cardView.scaleX = 1.15
      cardView.scaleY = 1.15

    cardView.onMouseDragged = event =>
      cardView.translateX = event.sceneX - dragContextX
      cardView.translateY = event.sceneY - dragContextY
      onCardDragged(event.sceneX, event.sceneY)

    cardView.onMouseReleased = event =>
      onCardDropped(card, event.sceneX, event.sceneY)

      val returnMove = new TranslateTransition(Duration(200), cardView):
        toX = 0
        toY = 0

      val returnScale = new ScaleTransition(Duration(200), cardView):
        toX = 1.0
        toY = 1.0

      val returnAnimation = new ParallelTransition { children = Seq(returnMove, returnScale) }
      returnAnimation.onFinished = _ => cardView.delegate.setViewOrder(0.0)
      returnAnimation.play()

    cardView
