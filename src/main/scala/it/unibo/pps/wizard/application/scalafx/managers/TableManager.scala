package it.unibo.pps.wizard.application.scalafx.managers

import it.unibo.pps.wizard.application.scalafx.components.PlayedCardWrapper
import it.unibo.pps.wizard.engine.model.basic.{Card, PlayerId, Table}
import scalafx.scene.layout.HBox

class TableManager(val container: HBox):
  private val normalStyle =
    "-fx-background-color: rgba(43, 92, 63, 0.85); -fx-background-radius: 15;"
  private val hoverStyle =
    "-fx-background-color: rgba(60, 120, 80, 0.95); -fx-background-radius: 15;"

  private var activeCardNodes: Map[Card, PlayedCardWrapper] = Map.empty

  def initializeTable(table: Table, winningCard: Option[Card]): Unit =
    container.children.clear()
    activeCardNodes = Map.empty
    table.playedCards.foreach(c => addCard(c, table.playerOf(c).get, winningCard.contains(c)))

  def addCard(card: Card, playerId: PlayerId, isWinningCard: Boolean): Unit =
    val cardWrapperNode = PlayedCardWrapper(card, playerId.toString, isWinningCard)
    cardWrapperNode.prefWidth <== container.width * 0.30
    activeCardNodes = activeCardNodes + (card -> cardWrapperNode)
    container.children.add(cardWrapperNode)

  // todo: winning and following card
  def updateCard(card: Card, isWinningCard: Boolean, isFollowingCard: Boolean): Unit =
    ???

  def setHighlight(active: Boolean): Unit =
    container.style = if (active) hoverStyle else normalStyle

  def isOver(sceneX: Double, sceneY: Double): Boolean =
    val bounds = container.localToScene(container.boundsInLocal.value)
    bounds.contains(sceneX, sceneY)
