package it.unibo.pps.wizard.application.scalafx.components

import it.unibo.pps.wizard.application.scalafx.util.WizardTheme
import it.unibo.pps.wizard.engine.model.basic.Card
import it.unibo.pps.wizard.engine.model.basic.Card.Color
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

class PlayedCardWrapper(
    val card: Card,
    playerName: String,
    isWinning: Boolean,
    isFollowing: Boolean
) extends VBox:
  alignment = Pos.Center
  spacing = 8
  style = WizardTheme.Table.cardWrapperStyle

  val cardView = new CardView(card)

  cardView.prefWidth <== this.width
  cardView.prefHeight <== cardView.prefWidth * 1.4

  val nameLabel: Label = new Label(playerName):
    style = s"-fx-text-fill: ${WizardTheme.Colors.textSoft}; -fx-font-weight: bold; -fx-font-size: 30px;"

  children = Seq(cardView, nameLabel)

  updateStatus(isWinning, isFollowing)

  def updateStatus(isWinning: Boolean, isFollowing: Boolean): Unit =
    style = if isWinning then WizardTheme.Table.winningCardWrapperStyle else WizardTheme.Table.cardWrapperStyle
    cardColor.filter(_ => isFollowing) match
      case Some(color) => cardView.setGlow(CardView.fxColor(color))
      case None        => cardView.removeGlow()

  private def cardColor: Option[Color] = card match
    case Card.Standard(color, _) => Some(color)
    case _                       => None
