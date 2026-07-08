package it.unibo.pps.wizard.application.gui.components

import it.unibo.pps.wizard.engine.model.basic.{Bid, Player, Card}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.control.{Label, TextField, ComboBox}
import scalafx.scene.layout.VBox
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle
import scalafx.scene.text.{Font, FontWeight}
import scalafx.collections.ObservableBuffer

abstract class BasePlayerView(val player: Player, val isCurrentTurn: Boolean = false) extends VBox:
  alignment = Pos.Center
  spacing = 6
  padding = Insets(8, 15, 8, 15)

  protected val normalStyle =
    "-fx-background-color: rgba(45, 52, 54, 0.7); -fx-background-radius: 10; -fx-border-color: #636e72; -fx-border-width: 1; -fx-border-radius: 10;"
  protected val activeTurnStyle =
    "-fx-background-color: rgba(230, 126, 34, 0.2); -fx-background-radius: 10; -fx-border-color: #e67e22; -fx-border-width: 2; -fx-border-radius: 10;"

  style = if isCurrentTurn then activeTurnStyle else normalStyle

  protected val avatarIndicator = new Circle:
    radius = 12
    fill = if isCurrentTurn then Color.rgb(230, 126, 34) else Color.rgb(178, 190, 195)

  protected val nameLabel = new Label(player.name.toString):
    font = Font.font("Arial", FontWeight.Bold, 15)
    textFill = Color.White

  protected var bidLabel = new Label(s"Bid: 0"):
    font = Font.font("Arial", FontWeight.Normal, 13)
    textFill = Color.rgb(178, 190, 195)

  def updateBid(bid: Bid): Unit =
    bidLabel.text = s"Bid: ${bid.value}"

class BotPlayerView(player: Player, isCurrentTurn: Boolean = false)
    extends BasePlayerView(player, isCurrentTurn):
  private val roleLabel = new Label("Bot"):
    font = Font.font("Arial", FontWeight.Normal, 13)
    textFill = Color.rgb(140, 140, 140)

  children = Seq(avatarIndicator, nameLabel, roleLabel, bidLabel)

class HumanPlayerView(
    player: Player,
    isCurrentTurn: Boolean = false,
    onBidSubmitted: Bid => Unit = _ => (),
    onTrumpSelected: Card.Color => Unit = _ => ()
) extends BasePlayerView(player, isCurrentTurn):

  private val roleLabel = new Label("You"):
    font = Font.font("Arial", FontWeight.Normal, 13)
    textFill = Color.rgb(230, 126, 34)

  val bidField = new TextField:
    promptText = "Bid"
    maxWidth = 60
    prefWidth = 60
    font = Font.font("Arial", 13)
    disable = !isCurrentTurn

    onAction = _ =>
      val textValue = text.value
      if textValue.nonEmpty && textValue.forall(_.isDigit) then onBidSubmitted(Bid(textValue.toInt))

  val trumpComboBox = new ComboBox[Card.Color]:
    items = ObservableBuffer(Card.Color.values.toSeq*)
    promptText = "Trump"
    maxWidth = 90
    disable = true

    onAction = _ => Option(selectionModel.value.getSelectedItem).foreach(onTrumpSelected)

  def setTrumpSelectionEnabled(enabled: Boolean): Unit =
    trumpComboBox.disable = !enabled

  children = Seq(
    avatarIndicator,
    nameLabel,
    roleLabel,
    bidLabel,
    bidField,
    trumpComboBox
  )
