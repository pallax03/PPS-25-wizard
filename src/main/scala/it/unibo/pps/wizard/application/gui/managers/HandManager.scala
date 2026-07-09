package it.unibo.pps.wizard.application.gui.managers

import it.unibo.pps.wizard.application.gui.components.CardView
import it.unibo.pps.wizard.engine.model.basic.{Card, Hand}
import scalafx.Includes.jfxMouseEvent2sfx
import scalafx.animation.{ParallelTransition, ScaleTransition, TranslateTransition}
import scalafx.scene.layout.HBox
import scalafx.util.Duration

class HandManager(
    val container: HBox,
    onCardDragged: (Double, Double) => Unit,
    onCardDropped: (Card, Double, Double) => Unit
):
  private var activeCardNodes: Map[Card, CardView] = Map.empty

  def updateHand(hand: Hand): Unit =
    container.children.clear()
    activeCardNodes = Map.empty
    hand.toList.foreach(addCard)

  private def addCard(card: Card): Unit =
    val cardNode = createDraggableCard(card)
    activeCardNodes = activeCardNodes + (card -> cardNode)
    container.children.add(cardNode)

  private def createDraggableCard(card: Card): CardView =
    val cardView = new CardView(card)

    cardView.prefWidth = 130.0
    cardView.prefHeight = 110.0

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

  def removeCard(card: Card): Unit =
    activeCardNodes
      .get(card)
      .foreach: node =>
        container.children.remove(node)
        activeCardNodes = activeCardNodes - card

  def hideLegitCards(legitCards: List[Card]): Unit =
    ???
