package it.unibo.pps.wizard.application.scalafx.components

import it.unibo.pps.wizard.application.scalafx.util.WizardTheme
import it.unibo.pps.wizard.engine.model.basic.cards.Card
import scalafx.geometry.Pos
import scalafx.scene.control.Label
import scalafx.scene.layout.VBox

/**
 * A wrapper for displaying a played card along with the player's name.
 *
 * @param card the card that was played
 * @param playerName the name of the player who played the card
 * @param isWinning indicates if the card is currently winning
 * @param isFollowing indicates if the card is following the leading suit
 */
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
    style =
      s"-fx-font-weight: bold; -fx-font-size: 30px;"
    textFill = WizardTheme.Colors.white

  children = Seq(cardView, nameLabel)

  updateStatus(isWinning, isFollowing)

  def updateStatus(isWinning: Boolean, isFollowing: Boolean): Unit =
    style =
      if isWinning then WizardTheme.Table.winningCardWrapperStyle
      else WizardTheme.Table.cardWrapperStyle
    cardColor.filter(_ => isFollowing) match
      case Some(color) => cardView.setGlow(CardView.fxColor(color))
      case None        => cardView.removeGlow()

  private def cardColor: Option[Card.Color] = card match
    case Card.Standard(color, _) => Some(color)
    case _                       => None
