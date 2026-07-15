package it.unibo.pps.wizard.application.scalafx.managers

import it.unibo.pps.wizard.application.scalafx.components.CardView
import it.unibo.pps.wizard.engine.model.basic.{Card, Hand}
import scalafx.Includes.jfxMouseEvent2sfx
import scalafx.animation.{ParallelTransition, ScaleTransition, TranslateTransition}
import scalafx.scene.layout.HBox
import scalafx.scene.paint.Color
import scalafx.util.Duration

class HandManager(
    val container: HBox,
    onCardDragged: (Double, Double) => Unit,
    onCardDropped: (Card, Double, Double) => Unit
):
  private var activeCardNodes: Map[Card, CardView] = Map.empty
  private val cardPrefWidth = 130.0

  container.width.onChange { (_, _, _) => adjustSpacing() }

  private def adjustSpacing(): Unit =
    val n = activeCardNodes.size
    if n > 1 then
      val totalCardsWidth = n * cardPrefWidth
      val availableWidth = container.width.value

      if availableWidth > 0 && totalCardsWidth > availableWidth then
        container.spacing = (availableWidth - totalCardsWidth) / (n - 1)
      else container.spacing = 5.0
    else container.spacing = 0.0

  def updateHand(hand: Hand): Unit =
    container.children.clear()
    activeCardNodes = Map.empty
    hand.toList.foreach(addCard)

  private def addCard(card: Card): Unit =
    val cardNode = createDraggableCard(card)
    activeCardNodes = activeCardNodes + (card -> cardNode)
    container.children.add(cardNode)
    adjustSpacing()

  private def createDraggableCard(card: Card): CardView =
    val cardView = new CardView(card)
    cardView.prefWidth = cardPrefWidth

    var dragContextX = 0.0
    var dragContextY = 0.0

    cardView.onMousePressed = event =>
      dragContextX = event.sceneX - cardView.translateX.value
      dragContextY = event.sceneY - cardView.translateY.value

      cardView.scaleX = 1.6
      cardView.scaleY = 1.6

    cardView.onMouseDragged = event =>
      val deltaY = math.abs(event.sceneY - dragContextY)
      cardView.translateX = event.sceneX - dragContextX
      cardView.translateY = event.sceneY - dragContextY
      if deltaY > 100 then
        cardView.scaleX = 3
        cardView.scaleY = 3
      else
        cardView.scaleX = 1.8
        cardView.scaleY = 1.8
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

  def highlightLegalCards(legalCards: List[Card]): Unit =
    activeCardNodes.foreach { case (card, node) =>
      if legalCards.contains(card) then node.opacity = 1.0
      else node.opacity = 0.4
    }

  def highlightWinningCard(card: Card): Unit =
    this.activeCardNodes(card).setGlow(Color.Gold)

  def clearEffects(): Unit =
    this.activeCardNodes.values.foreach(node => {
      node.opacity = 1.0
      node.removeGlow()
    })
