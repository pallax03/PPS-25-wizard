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

//  extension(c: Card)
//    private def isFollowingColor(color: Option[Color]): Boolean = c match
//      case Standard(c, _) => color match
//        case Some(value) => value == c
//        case _ => false
//      case _ => false
//
  def addCard(card: Card, playerName: PlayerName, isWinning: Boolean, isFollowing: Boolean): Unit =
    val cardWrapperNode = PlayedCardWrapper(card, playerName.toString, isWinning, isFollowing)
    cardWrapperNode.prefWidth <== container.width * 0.30
    activeCardNodes = activeCardNodes + (card -> cardWrapperNode)
    container.children.add(cardWrapperNode)

  // todo: update old cards when isWinningCard, IsFollowingCard
  def updateCard(card: Card, isWinningCard: Boolean, isFollowingCard: Boolean): Unit =
    ???

  def setHighlight(active: Boolean): Unit =
    container.style = if (active) hoverStyle else normalStyle

  def isOver(sceneX: Double, sceneY: Double): Boolean =
    val bounds = container.localToScene(container.boundsInLocal.value)
    bounds.contains(sceneX, sceneY)
