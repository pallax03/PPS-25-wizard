package it.unibo.pps.wizard.application.scalafx.controllers.gameboard

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.components.*
import it.unibo.pps.wizard.application.scalafx.controllers.Controller
import it.unibo.pps.wizard.application.scalafx.managers.{
  HandManager,
  OpponentsManager,
  TableManager,
  TrumpManager
}
import it.unibo.pps.wizard.application.scalafx.pages.{MainPage, ScoreboardPage}
import it.unibo.pps.wizard.application.scalafx.util.{UiPhase, WizardTheme}
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.basic.Card.*
import it.unibo.pps.wizard.engine.model.core.GameAction.PlayCard
import it.unibo.pps.wizard.engine.model.core.GameAction
import javafx.animation.{ParallelTransition, ScaleTransition, TranslateTransition}
import javafx.event.ActionEvent as JfxActionEvent
import javafx.scene.control.{Alert as JfxAlert, Button, ButtonType as JfxButtonType}
import javafx.scene.layout.{HBox, StackPane, VBox}
import scalafx.scene.Node
import scalafx.scene.control.Label
import scalafx.stage.{Modality, Stage}
import scalafx.util.Duration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

class GameBoardPageController(stage: Stage, currentPlayerId: PlayerId)(using
    context: WizardApplicationContext
) extends Controller(stage)
    with GameBoardView:

  @nowarn @FXML private var tableContainer: HBox = _
  @nowarn @FXML private var handContainer: HBox = _
  @nowarn @FXML private var trumpContainer: StackPane = _
  @nowarn @FXML private var currentPlayerContainer: VBox = _
  @nowarn @FXML private var playersContainer: HBox = _
  @nowarn @FXML private var gameInfoContainer: VBox = _
  @nowarn @FXML private var scoreboardContainer: StackPane = _
  @nowarn @FXML private var rulesPanel: VBox = _
  @nowarn @FXML private var hintBestCardButton: Button = _
  @nowarn @FXML private var messageNotificationContainer: HBox = _

  @nowarn private var tableManager: TableManager = _
  @nowarn private var handManager: HandManager = _
  @nowarn private var opponentsManager: OpponentsManager = _
  @nowarn private var trumpManager: TrumpManager = _
  @nowarn private var currentPlayerView: HumanPlayerView = _
  @nowarn private var gameInfo: GameInfoView = _
  @nowarn private var activeScoreboardStage: Stage = _
  @nowarn private var activeScoreboardPage: ScoreboardPage = _
  @nowarn private var gameBoardDispatcher: GameBoardEventDispatcher = _
  @nowarn private var currentMessageLabel: MessageLabel = _

  @FXML
  def initialize(): Unit =
    List(
      tableContainer,
      handContainer,
      trumpContainer,
      currentPlayerContainer,
      playersContainer,
      messageNotificationContainer
    )
      .foreach(_.getChildren.clear())
    buildUI()
    this.gameBoardDispatcher = GameBoardEventDispatcher(this)
    this.gameBoardDispatcher.startListening()

  private def buildUI(): Unit =
    this.tableManager = TableManager(this.tableContainer)
    this.trumpManager = TrumpManager(
      this.trumpContainer,
      onColorSelected = color =>
        context.inboundPort.submitAction(
          GameAction.ResolveTrumpColor(currentPlayerId, color)
        )
    )

    this.handManager = HandManager(
      this.handContainer,
      onCardDragged =
        (mouseX, mouseY) => tableManager.setHighlight(tableManager.isOver(mouseX, mouseY)),
      onCardDropped = (card, mouseX, mouseY) =>
        tableManager.setHighlight(false)
        if tableManager.isOver(mouseX, mouseY) then
          context.inboundPort.submitAction(PlayCard(currentPlayerId, card))
    )

    this.opponentsManager = OpponentsManager(playersContainer)

    this.gameInfo = GameInfoView()
    this.gameInfoContainer.getChildren.add(this.gameInfo.delegate)

    activeScoreboardStage = new Stage():
      initModality(Modality.None)
      title = "Scoreboard"
      resizable = false
      onCloseRequest = _ => activeScoreboardStage.hide()

    activeScoreboardPage = ScoreboardPage(activeScoreboardStage)

  override def getCurrentPlayerId: PlayerId = currentPlayerId

  override def displayGameStarted(players: Players): Unit =
    this.opponentsManager.renderAllOpponents(players.filter(_.id != currentPlayerId))
    val currentPlayer = players.findById(currentPlayerId).get
    this.currentPlayerView = HumanPlayerView(
      currentPlayer,
      onBidSubmitted =
        bid => context.inboundPort.submitAction(GameAction.PlaceBid(currentPlayerId, bid)),
    )
    this.currentPlayerContainer.getChildren.add(this.currentPlayerView.delegate)
    this.activeScoreboardPage.initializeTable(players)

  override def displayWaitingForTrump(playerId: PlayerId): Unit =
    if playerId == currentPlayerId then trumpManager.enableResolveTrumpColor(true)

  override def displayTrickWon(
      winnerId: PlayerId,
      tricksWon: Trick,
      trickedCards: List[Card]
  ): Unit =
    val (messageText, messageColor) =
      if winnerId == currentPlayerId then ("Hai vinto la mano!", WizardTheme.Colors.winning)
      else (s"Mano vinta da Bot $winnerId", WizardTheme.Colors.winning)

    displayTemporaryMessage(messageText, messageColor)
    if winnerId == currentPlayerId then this.currentPlayerView.updateTricksWon(tricksWon)
    else this.opponentsManager.updateOpponentsTricksWon(winnerId, tricksWon)

  override def clearTable(): Unit =
    tableManager.initialize()

  override def displayPhaseChanged(phase: String): Unit =
    val uiPhase = UiPhase.fromName(phase)
    this.gameInfo.changePhase(uiPhase)
    if !uiPhase.isBidding then currentPlayerView.setBidTextFieldEnabled(false)

  override def displayCardsDealt(
      playerId: PlayerId,
      hands: Hands,
      trump: Trump,
      round: Round
  ): Unit =
    this.gameInfo.incrementRound(round.value)
    this.trumpManager.initialize(trump)
    val newHand = hands.getHand(currentPlayerId).getOrElse(Hand.empty)
    this.handManager.updateHand(newHand)
    this.currentPlayerView.setMaxBid(round.value)

  override def displayCardPlayed(
      playerId: PlayerId,
      playerName: PlayerName,
      card: Card,
      winningCard: Option[Card],
      followingColor: Option[Card.Color]
  ): Unit =
    val tablePlayerName = if playerId == currentPlayerId then PlayerName("You") else playerName
    tableManager.addCard(card, tablePlayerName, winningCard, followingColor)
    handManager.removeCard(card)
    handManager.clearEffects()
    setVisibleNode(hintBestCardButton)(false)

  override def displayTrumpSelected(playerId: PlayerId, color: Card.Color): Unit =
    trumpManager.glowTrump(color)
    trumpManager.enableResolveTrumpColor(false)

  override def displayBidPlaced(playerId: PlayerId, bid: Bid): Unit =
    if playerId != currentPlayerId then this.opponentsManager.updateOpponentBid(playerId, bid)

  override def displayTurnChanged(nextPlayerId: PlayerId, phase: UiPhase): Unit =
    val isMyTurn = nextPlayerId == currentPlayerId
    setVisibleNode(hintBestCardButton)(isMyTurn && phase.isPlaying)
    this.currentPlayerView.setTurnActive(isMyTurn, phase)
    this.currentPlayerView.setBidTextFieldEnabled(isMyTurn && phase.isBidding)
    this.opponentsManager.updateActiveTurn(nextPlayerId, phase)

  override def displayRoundScored(scoreboard: Scoreboard, players: Players): Unit =
    refreshScoreboardIfOpen(scoreboard, players)
    this.currentPlayerView.resetBid()
    this.opponentsManager.resetOpponentsBid()

  override def displayLegalCards(playerId: PlayerId, legalCards: List[Card]): Unit =
    if playerId == currentPlayerId then this.handManager.highlightLegalCards(legalCards)

  override def displayGameEnded(scoreboard: Scoreboard, players: Players): Unit =
    refreshScoreboardIfOpen(scoreboard, players)
    displayGameEndedAlert()

  override def displayErrorMessage(message: String): Unit =
    displayTemporaryMessage(message, WizardTheme.Colors.warning)

  private def displayTemporaryMessage(message: String, color: String): Unit =
    if messageNotificationContainer != null then
      if currentMessageLabel != null then
        currentMessageLabel.cancel()
        messageNotificationContainer.getChildren.remove(currentMessageLabel)

      val messageLabel = MessageLabel()
      currentMessageLabel = messageLabel

      messageNotificationContainer.getChildren.add(messageLabel)

      messageLabel.show(
        message,
        color,
        onFinishedAction = {
          messageNotificationContainer.getChildren.remove(messageLabel)
          if currentMessageLabel == messageLabel then currentMessageLabel = null
        }
      )

  private def displayGameEndedAlert(): Unit =
    val showScoreboardButton = JfxButtonType("Show Scoreboard")
    val returnToMenuButton = JfxButtonType("Return to Main Menu")

    val alert = JfxAlert(JfxAlert.AlertType.INFORMATION)
    alert.initOwner(stage.delegate)
    alert.setTitle("Game Over")
    alert.setHeaderText("The game has ended!")
    alert.setContentText("You can inspect the final scoreboard or return to the main menu.")
    alert.getButtonTypes.setAll(showScoreboardButton, returnToMenuButton)

    alert.getDialogPane
      .lookupButton(showScoreboardButton)
      .addEventFilter(
        JfxActionEvent.ACTION,
        event =>
          openScoreboardWindow()
          event.consume()
      )

    alert.setOnHidden(_ => if alert.getResult == returnToMenuButton then returnToMainMenu())
    alert.show()

  private def returnToMainMenu(): Unit =
    gameBoardDispatcher.stopListening()
    activeScoreboardStage.close()
    MainPage(stage)

  override def displayShowInvalidBid(): Unit =
    currentPlayerView.showErrorEffect(true)
  override def displayClearInvalidBid(): Unit =
    currentPlayerView.showErrorEffect(false)

  @FXML
  def openScoreboardWindow(): Unit =
    activeScoreboardStage match
      case stg if stg.isShowing =>
        stg.toFront()
      case _ =>
        activeScoreboardStage.show()

  private def refreshScoreboardIfOpen(scoreboard: Scoreboard, players: Players): Unit =
    val rows = RoundRow.updateRows(players, scoreboard)
    activeScoreboardPage.updateData(rows, players.toList.size)

  @FXML
  def handleScoreboardHover(): Unit =
    if scoreboardContainer != null then
      val scale = new ScaleTransition(Duration(120), scoreboardContainer)
      scale.setToX(1.15)
      scale.setToY(1.15)

      val translate = new TranslateTransition(Duration(120), scoreboardContainer)
      translate.setToX(-5)

      val parallel = new ParallelTransition(scale, translate)
      parallel.play()

  @FXML
  def handleScoreboardExit(): Unit =
    if scoreboardContainer != null then
      val scale = new ScaleTransition(Duration(120), scoreboardContainer)
      scale.setToX(1.0)
      scale.setToY(1.0)

      val translate = new TranslateTransition(Duration(120), scoreboardContainer)
      translate.setToX(0)

      val parallel = new ParallelTransition(scale, translate)
      parallel.play()

  @FXML
  def toggleRulesPanel(): Unit =
    setVisibleNode(rulesPanel)(!rulesPanel.isVisible)

  @FXML
  def requestHintBestCard(): Unit =
    context.hintPort
      .bestCard(currentPlayerId)
      .onComplete:
        case Success(card) => this.handManager.highlightWinningCard(card)
        case _             => this.handManager.clearEffects()

  private def setVisibleNode(node: Node)(enabled: Boolean): Unit =
    if node != null then
      node.managed = enabled
      node.visible = enabled
