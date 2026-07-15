package it.unibo.pps.wizard.application.scalafx.managers

import it.unibo.pps.wizard.application.scalafx.components.PlayedCardWrapper
import it.unibo.pps.wizard.application.scalafx.util.WizardTheme
//import it.unibo.pps.wizard.engine.model.basic.Card.*
import it.unibo.pps.wizard.engine.model.basic.{Card, PlayerName}
import scalafx.scene.layout.HBox

class TableManager(val container: HBox):
  private val normalStyle = WizardTheme.Table.normalStyle
  private val hoverStyle = WizardTheme.Table.hoverStyle

  private var activeCardNodes: Map[Card, PlayedCardWrapper] = Map.empty

  def initialize(): Unit =
    container.children.clear()
    activeCardNodes = Map.empty

  def addCard(
      card: Card,
      playerName: PlayerName,
      winningCard: Option[Card],
      followingColor: Option[Card.Color]
  ): Unit =
    val cardWrapperNode = PlayedCardWrapper(
      card,
      playerName.toString,
      isWinningCard(card, winningCard),
      isFollowingCard(card, followingColor)
    )
    cardWrapperNode.prefWidth <== container.width * 0.30
    activeCardNodes = activeCardNodes + (card -> cardWrapperNode)
    container.children.add(cardWrapperNode)
    refreshCards(winningCard, followingColor)

  def updateCard(card: Card, isWinningCard: Boolean, isFollowingCard: Boolean): Unit =
    activeCardNodes.get(card).foreach(_.updateStatus(isWinningCard, isFollowingCard))

  def setHighlight(active: Boolean): Unit =
    container.style = if (active) hoverStyle else normalStyle

  def isOver(sceneX: Double, sceneY: Double): Boolean =
    val bounds = container.localToScene(container.boundsInLocal.value)
    bounds.contains(sceneX, sceneY)

  private def refreshCards(winningCard: Option[Card], followingColor: Option[Card.Color]): Unit =
    activeCardNodes.foreach: (card, view) =>
      view.updateStatus(isWinningCard(card, winningCard), isFollowingCard(card, followingColor))

  private def isWinningCard(card: Card, winningCard: Option[Card]): Boolean =
    winningCard.contains(card)

  private def isFollowingCard(card: Card, followingColor: Option[Card.Color]): Boolean =
    (card, followingColor) match
      case (Card.Standard(color, _), Some(requiredColor)) => color == requiredColor
      case _                                             => false
