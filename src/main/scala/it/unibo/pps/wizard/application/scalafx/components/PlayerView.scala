package it.unibo.pps.wizard.application.scalafx.components

import it.unibo.pps.wizard.engine.model.basic.{Bid, Card, Player}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{ComboBox, Label, TextField}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scalafx.collections.ObservableBuffer

abstract class BasePlayerView(val player: Player, val isCurrentTurn: Boolean = false) extends VBox:
  alignment = Pos.Center
  spacing = 6
  padding = Insets(8, 15, 8, 15)

  protected val normalStyle =
    "-fx-background-color: rgba(45, 52, 54, 0.7); -fx-background-radius: 10; -fx-border-color: #636e72; -fx-border-width: 1; -fx-border-radius: 10;"
  private val biddingTurnStyle =
    "-fx-background-color: rgba(230, 126, 34, 0.2);-fx-background-radius: 10; -fx-border-color: #e67e22; -fx-border-width: 2; -fx-border-radius: 10;"
  private val playingTurnStyle =
    "-fx-background-color: rgba(46, 204, 113, 0.2); -fx-background-radius: 10; -fx-border-color: #2ecc71; -fx-border-width: 2; -fx-border-radius: 10;"

  style = if isCurrentTurn then biddingTurnStyle else normalStyle

  protected val nameLabel: Label = new Label(player.name.toString):
    font = Font.font("Arial", FontWeight.Bold, 15)
    textFill = Color.White

  protected val tricksWonLabel: Label = new Label(s"Tricks Won: 0"):
    font = Font.font("Arial", FontWeight.Normal, 13)
    textFill = Color.rgb(178, 190, 195)

  protected var bidLabel: Label = new Label(s"Bid: -"):
    font = Font.font("Arial", FontWeight.Normal, 13)
    textFill = Color.rgb(178, 190, 195)

  def updateBid(bid: String): Unit =
    bidLabel.text = s"Bid: $bid"

  def resetBid(): Unit = {
    tricksWonLabel.text = s"Tricks Won: 0"
    bidLabel.text = s"Bid: -"
  }

  def setTurnActive(active: Boolean = true, phase: String): Unit =
    style =
      if active then
        phase match
          case "ChoosingTrump" => biddingTurnStyle
          case "Bidding"       => biddingTurnStyle
          case "Playing"       => playingTurnStyle
      else normalStyle

  def updateTricksWon(tricks: Int): Unit =
    tricksWonLabel.text = s"Tricks Won: $tricks"

class BotPlayerView(player: Player, isCurrentTurn: Boolean = false)
    extends BasePlayerView(player, isCurrentTurn):
  private val roleLabel = new Label("Bot"):
    font = Font.font("Arial", FontWeight.Normal, 13)
    textFill = Color.rgb(140, 140, 140)

  private val name = new HBox():
    alignment = Pos.Center
    spacing = 5
    children = Seq(nameLabel, roleLabel)

  children = Seq(name, tricksWonLabel, bidLabel)

class HumanPlayerView(
    player: Player,
    isCurrentTurn: Boolean = false,
    onBidSubmitted: Bid => Unit = _ => (),
    onTrumpSelected: Card.Color => Unit = _ => ()
) extends BasePlayerView(player, isCurrentTurn):

  private val roleLabel = new Label("You"):
    font = Font.font("Arial", FontWeight.Normal, 13)
    textFill = Color.rgb(230, 126, 34)

  private val bidField = new TextField:
    promptText = "Bid"
    maxWidth = 60
    prefWidth = 60
    font = Font.font("Arial", 13)
    disable = !isCurrentTurn

    onAction = _ =>
      val textValue = text.value
      if textValue.nonEmpty && textValue.forall(_.isDigit) then onBidSubmitted(Bid(textValue.toInt))

  private val trumpComboBox = new ComboBox[Card.Color]:
    items = ObservableBuffer(Card.Color.values.toSeq*)
    promptText = "Trump"
    maxWidth = 90
    disable = true

    onAction = _ => Option(selectionModel.value.getSelectedItem).foreach(onTrumpSelected)

  def setTrumpSelectionEnabled(enabled: Boolean): Unit =
    trumpComboBox.disable = !enabled

  def setBidTextFieldEnabled(enabled: Boolean): Unit =
    bidField.disable = !enabled

  private val name = new HBox():
    alignment = Pos.Center
    spacing = 5
    children = Seq(nameLabel, roleLabel)

  children = Seq(
    name,
    tricksWonLabel,
    bidLabel,
    bidField,
    trumpComboBox
  )
