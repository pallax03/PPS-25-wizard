package it.unibo.pps.wizard.application.scalafx.components

import it.unibo.pps.wizard.engine.model.basic.{Bid, Player}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.{Label, TextField}
import scalafx.scene.layout.{HBox, Priority, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}
import scala.compiletime.uninitialized

abstract class BasePlayerView(val player: Player, val isCurrentTurn: Boolean = false) extends VBox:
  alignment = Pos.Center
  spacing = 0

  private val cornerRadii = "-fx-background-radius: 10; -fx-border-radius: 10;"

  protected val normalStyle = s"-fx-background-color: rgba(45, 52, 54, 0.7); $cornerRadii -fx-border-color: #636e72; -fx-border-width: 2;"
  private val biddingTurnStyle = s"-fx-background-color: rgba(230, 126, 34, 0.2); $cornerRadii -fx-border-color: #e67e22; -fx-border-width: 2;"
  private val playingTurnStyle = s"-fx-background-color: rgba(46, 204, 113, 0.2); $cornerRadii -fx-border-color: #2ecc71; -fx-border-width: 2;"

  style = if isCurrentTurn then biddingTurnStyle else normalStyle

  protected val nameLabel: Label = new Label(player.name.toString):
    font = Font.font("Arial", FontWeight.Bold, 15)
    textFill = Color.White

  protected val roleLabel: Label = new Label():
    font = Font.font("Arial", FontWeight.Bold, 13)

  private val tricksTitleLabel: Label = new Label("Tricks Won"):
    font = Font.font("Arial", FontWeight.Normal, 13)
    textFill = Color.rgb(178, 190, 195)

  protected val tricksWonLabel: Label = new Label("0"):
    font = Font.font("Arial", FontWeight.Bold, 13)
    textFill = Color.White

  private val bidTitleLabel: Label = new Label("Bids"):
    font = Font.font("Arial", FontWeight.Normal, 13)
    textFill = Color.rgb(178, 190, 195)

  protected var bidValueNode: Node = uninitialized
  protected var row3: HBox = uninitialized

  def updateBid(bid: String): Unit
  def resetBid(): Unit

  def setTurnActive(active: Boolean = true, phase: String): Unit =
    style =
      if active then
        phase match
          case "ChoosingTrump" => biddingTurnStyle
          case "Bidding"       => biddingTurnStyle
          case "Playing"       => playingTurnStyle
      else normalStyle

  def updateTricksWon(tricks: Int): Unit =
    tricksWonLabel.text = tricks.toString

  protected def buildUI(): Unit =
    val row1 = createRow(nameLabel, roleLabel, hasBottomBorder = true)
    val row2 = createRow(tricksTitleLabel, tricksWonLabel, hasBottomBorder = true)
    row3 = createRow(bidTitleLabel, bidValueNode, hasBottomBorder = false)
    // Assicuriamo che gli angoli inferiori della riga 3 rispettino l'arrotondamento
    row3.style = row3.style.value + " -fx-background-radius: 0 0 8 8;"

    children = Seq(row1, row2, row3)

  private def createRow(leftNode: Node, rightNode: Node, hasBottomBorder: Boolean): HBox =
    val leftCell = new HBox:
      alignment = Pos.CenterLeft
      padding = Insets(8, 15, 8, 15)
      children = leftNode
      hgrow = Priority.Always
      prefWidth = 140
      style = "-fx-border-color: #636e72; -fx-border-width: 0 1 0 0;" // Linea verticale separatrice

    val rightCell = new HBox:
      alignment = Pos.Center
      padding = Insets(8, 10, 8, 10)
      children = rightNode
      prefWidth = 60

    new HBox:
      style = if hasBottomBorder then "-fx-border-color: #636e72; -fx-border-width: 0 0 1 0;" else ""
      children = Seq(leftCell, rightCell)


class BotPlayerView(player: Player, isCurrentTurn: Boolean = false)
  extends BasePlayerView(player, isCurrentTurn):

  roleLabel.text = "Bot"
  roleLabel.textFill = Color.rgb(140, 140, 140)

  private val bidLabelDisplay = new Label("-"):
    font = Font.font("Arial", FontWeight.Bold, 13)
    textFill = Color.White

  bidValueNode = bidLabelDisplay

  override def updateBid(bid: String): Unit =
    bidLabelDisplay.text = bid

  override def resetBid(): Unit =
    tricksWonLabel.text = "0"
    bidLabelDisplay.text = "-"

  buildUI()


class HumanPlayerView(
                       player: Player,
                       isCurrentTurn: Boolean = false,
                       onBidSubmitted: Bid => Unit = _ => (),
                     ) extends BasePlayerView(player, isCurrentTurn):

  roleLabel.text = "You"
  roleLabel.textFill = Color.rgb(230, 126, 34)

  private var maxBidBound: Int = 10

  private val bidField = new TextField:
    promptText = "!"
    maxWidth = 45
    prefWidth = 45
    font = Font.font("Arial", 13)
    disable = !isCurrentTurn
    alignment = Pos.Center

    onAction = _ => submitBid()

  bidValueNode = bidField

  def setMaxBid(max: Int): Unit =
    maxBidBound = max
    bidField.promptText = s"0-$max"

  def setBidTextFieldEnabled(enabled: Boolean): Unit =
    bidField.disable = !enabled
    if enabled then
      row3.style = "-fx-background-color: rgba(230, 126, 34, 0.4); -fx-background-radius: 0 0 8 8;"
    else
      row3.style = "-fx-background-radius: 0 0 8 8;"
      bidField.style = ""

  private def submitBid(): Unit =
    val textValue = bidField.text.value
    if textValue.nonEmpty && textValue.forall(_.isDigit) then
      val bidValue = textValue.toInt
      if bidValue >= 0 && bidValue <= maxBidBound then
        bidField.style = ""
        onBidSubmitted(Bid(bidValue))

  def showErrorEffect(enabled: Boolean): Unit =
    if enabled then
      row3.style = "-fx-background-color: rgba(255, 0, 0, 0.4); -fx-background-radius: 0 0 8 8;"
      bidField.style = "-fx-border-color: red; -fx-border-width: 2; -fx-border-radius: 3;"
    else if !bidField.disable.value then
      row3.style = "-fx-background-color: rgba(230, 126, 34, 0.4); -fx-background-radius: 0 0 8 8;"
      bidField.style = ""

  override def updateBid(bid: String): Unit =
    bidField.text = bid
    setBidTextFieldEnabled(false)

  override def resetBid(): Unit =
    tricksWonLabel.text = "0"
    bidField.text = ""
    setBidTextFieldEnabled(false)
  buildUI()