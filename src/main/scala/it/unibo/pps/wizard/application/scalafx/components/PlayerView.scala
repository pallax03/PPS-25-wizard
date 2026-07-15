package it.unibo.pps.wizard.application.scalafx.components

import it.unibo.pps.wizard.application.scalafx.util.{UiPhase, WizardTheme}
import it.unibo.pps.wizard.engine.model.basic.{Bid, Player}
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.{Label, TextField}
import scalafx.scene.layout.{HBox, Priority, VBox}
import scalafx.scene.text.{Font, FontWeight}
import scala.compiletime.uninitialized

abstract class BasePlayerView(val player: Player, val isCurrentTurn: Boolean = false) extends VBox:
  alignment = Pos.Center
  spacing = 0

  protected val normalStyle = WizardTheme.Player.normalStyle

  style = if isCurrentTurn then WizardTheme.Player.activeStyle(UiPhase.Bidding) else normalStyle

  protected val nameLabel: Label = new Label(player.name.toString):
    font = Font.font("Arial", FontWeight.Bold, 15)
    textFill = WizardTheme.Colors.white

  protected val roleLabel: Label = new Label():
    font = Font.font("Arial", FontWeight.Bold, 13)

  private val tricksTitleLabel: Label = new Label("Tricks Won"):
    font = Font.font("Arial", FontWeight.Normal, 13)
    textFill = WizardTheme.Colors.textMuted

  protected val tricksWonLabel: Label = new Label("0"):
    font = Font.font("Arial", FontWeight.Bold, 13)
    textFill = WizardTheme.Colors.white

  private val bidTitleLabel: Label = new Label("Bids"):
    font = Font.font("Arial", FontWeight.Normal, 13)
    textFill = WizardTheme.Colors.textMuted

  protected var bidValueNode: Node = uninitialized
  protected var row3: HBox = uninitialized

  def updateBid(bid: String): Unit
  def resetBid(): Unit

  def setTurnActive(active: Boolean = true, phase: UiPhase): Unit =
    style = if active then WizardTheme.Player.activeStyle(phase) else normalStyle

  def updateTricksWon(tricks: Int): Unit =
    tricksWonLabel.text = tricks.toString

  protected def buildUI(): Unit =
    val row1 = createRow(nameLabel, roleLabel, hasBottomBorder = true)
    val row2 = createRow(tricksTitleLabel, tricksWonLabel, hasBottomBorder = true)
    row3 = createRow(bidTitleLabel, bidValueNode, hasBottomBorder = false)
    row3.style = row3.style.value + " " + WizardTheme.Player.rowBottomRadius

    children = Seq(row1, row2, row3)

  private def createRow(leftNode: Node, rightNode: Node, hasBottomBorder: Boolean): HBox =
    val leftCell = new HBox:
      alignment = Pos.CenterLeft
      padding = Insets(8, 15, 8, 15)
      children = leftNode
      hgrow = Priority.Always
      prefWidth = 140
      style = WizardTheme.Player.cellSeparatorStyle

    val rightCell = new HBox:
      alignment = Pos.Center
      padding = Insets(8, 10, 8, 10)
      children = rightNode
      prefWidth = 60

    new HBox:
      style = if hasBottomBorder then WizardTheme.Player.rowBorderStyle else ""
      children = Seq(leftCell, rightCell)

class BotPlayerView(player: Player, isCurrentTurn: Boolean = false)
    extends BasePlayerView(player, isCurrentTurn):

  roleLabel.text = "Bot"
  roleLabel.textFill = WizardTheme.Colors.roleBot

  private val bidLabelDisplay = new Label("-"):
    font = Font.font("Arial", FontWeight.Bold, 13)
    textFill = WizardTheme.Colors.white

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
    onBidSubmitted: Bid => Unit = _ => ()
) extends BasePlayerView(player, isCurrentTurn):

  roleLabel.text = "You"
  roleLabel.textFill = WizardTheme.Colors.roleHuman

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
    if enabled then row3.style = WizardTheme.Player.activeBidRowStyle
    else
      row3.style = WizardTheme.Player.rowBottomRadius
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
      row3.style = WizardTheme.Player.errorBidRowStyle
      bidField.style = WizardTheme.Player.errorBidFieldStyle
    else if !bidField.disable.value then
      row3.style = WizardTheme.Player.activeBidRowStyle
      bidField.style = ""

  override def updateBid(bid: String): Unit =
    bidField.text = bid
    setBidTextFieldEnabled(false)

  override def resetBid(): Unit =
    tricksWonLabel.text = "0"
    bidField.text = ""
    setBidTextFieldEnabled(false)
  buildUI()
