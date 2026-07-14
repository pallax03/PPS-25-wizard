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
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.basic.Card.*
import it.unibo.pps.wizard.engine.model.core.GameAction.PlayCard
import it.unibo.pps.wizard.engine.model.core.GameAction
import javafx.animation.{ParallelTransition, ScaleTransition, TranslateTransition}
import javafx.scene.layout.{HBox, StackPane, VBox}
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType}
import scalafx.stage.{Modality, Stage}
import scalafx.util.Duration

class GameBoardPageController(stage: Stage, currentPlayerId: PlayerId)(using
    context: WizardApplicationContext
) extends Controller(stage)
    with GameBoardView:

  @nowarn @FXML private var tableContainer: HBox = _
  @nowarn @FXML private var handContainer: HBox = _
  @nowarn @FXML private var trumpContainer: VBox = _
  @nowarn @FXML private var currentPlayerContainer: VBox = _
  @nowarn @FXML private var playersContainer: HBox = _
  @nowarn @FXML private var gameInfoContainer: VBox = _
  @nowarn @FXML private var scoreboardContainer: StackPane = _
  @nowarn @FXML private var rulesPanel: VBox = _

  @nowarn private var tableManager: TableManager = _
  @nowarn private var handManager: HandManager = _
  @nowarn private var opponentsManager: OpponentsManager = _
  @nowarn private var trumpManager: TrumpManager = _
  @nowarn private var currentPlayerView: HumanPlayerView = _
  @nowarn private var gameInfo: GameInfoView = _
  @nowarn private var activeScoreboardStage: Stage = _
  @nowarn private var activeScoreboardPage: ScoreboardPage = _
  @nowarn private var gameBoardDispatcher: GameBoardEventDispatcher = _

  @FXML
  def initialize(): Unit =
    println("Initializing GameBoardPageController...")
    List(tableContainer, handContainer, trumpContainer, currentPlayerContainer, playersContainer)
      .foreach(_.getChildren.clear())
    buildUI()
    this.gameBoardDispatcher = GameBoardEventDispatcher(this)
    this.gameBoardDispatcher.startListening()

  private def buildUI(): Unit =
    this.tableManager = TableManager(this.tableContainer)
    this.trumpManager = TrumpManager(this.trumpContainer)

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

  override def displayGameStarted(players: Players): Unit =
    println(s"Event received: Game started with players: $players")
    this.opponentsManager.renderAllOpponents(players.filter(_.id != currentPlayerId))
    val currentPlayer = players.findById(currentPlayerId).get
    this.currentPlayerView = HumanPlayerView(
      currentPlayer,
      onBidSubmitted =
        bid => context.inboundPort.submitAction(GameAction.PlaceBid(currentPlayerId, bid)),
      onTrumpSelected = color =>
        context.inboundPort.submitAction(
          GameAction.ResolveTrumpColor(currentPlayer.id, color)
        )
    )
    this.currentPlayerContainer.getChildren.add(this.currentPlayerView.delegate)
    this.activeScoreboardPage.initializeTable(players)

  override def displayWaitingForTrump(value: PlayerId): Unit =
    println(s"Event received: Waiting for Trump selection from player $value")
    if value == currentPlayerView.player.id then currentPlayerView.setTrumpSelectionEnabled(true)
    else currentPlayerView.setTrumpSelectionEnabled(false)

  override def displayTrickWon(winnerId: PlayerId, tricksWon: Int, trickedCards: List[Card]): Unit =
    println(s"Event received: Trick won by player $winnerId with cards: $trickedCards")
    tableManager.initializeTable(Table.empty, None)
    if winnerId == currentPlayerId then this.currentPlayerView.updateTricksWon(tricksWon.toString)
    else this.opponentsManager.updateOpponentsTricksWon(winnerId, tricksWon.toString)

  override def displayPhaseChanged(phase: String): Unit =
    this.gameInfo.changePhase(phase)
    if phase != "Bidding" then currentPlayerView.setBidTextFieldEnabled(false)

  override def displayCardsDealt(
      playerId: PlayerId,
      hands: Hands,
      trump: Trump,
      round: Round
  ): Unit =
    this.gameInfo.incrementRound(round.value)
    println(s"Event received: Cards dealt. Player: $playerId Trump: $trump Hands: $hands")
    this.trumpManager.initialize(trump)
    val newHand = hands.getHand(currentPlayerId).getOrElse(Hand.empty)
    this.handManager.updateHand(newHand)

  override def displayCardPlayed(playerId: PlayerId, card: Card): Unit =
    println(s"Event received: Card played by player $playerId: $card")
    tableManager.addCard(card, playerId, false)
    handManager.removeCard(card)

  override def displayTrumpSelected(playerId: PlayerId, color: Card.Color): Unit =
    println(s"Event received: Trump selected by player $playerId: $color")
    trumpManager.glowTrump(color)

  override def displayBidPlaced(playerId: PlayerId, bid: Bid): Unit =
    println(s"Event received: Bid placed by player $playerId: $bid")
    if playerId == currentPlayerId then this.currentPlayerView.updateBid(bid.toString)
    else this.opponentsManager.updateOpponentBid(playerId, bid)

  override def displayTurnChanged(nextPlayerId: PlayerId, phase: String): Unit =
    val isMyTurn = nextPlayerId == currentPlayerId
    this.currentPlayerView.setTurnActive(isMyTurn, phase)
    this.currentPlayerView.setBidTextFieldEnabled(isMyTurn && phase == "Bidding")
    this.opponentsManager.updateActiveTurn(nextPlayerId, phase)

  override def displayRoundScored(scoreboard: Scoreboard, players: Players): Unit =
    println(s"Event received: Round scored. Scoreboard: $scoreboard")
    refreshScoreboardIfOpen(scoreboard, players)
    this.currentPlayerView.resetBid()
    this.opponentsManager.resetOpponentsBid()

  override def displayLegalCards(playerId: PlayerId, legalCards: List[Card]): Unit =
    println(s"Event received: Legal cards for player $playerId: $legalCards")
    if playerId == currentPlayerId then
      this.handManager.highlightLegalCards(legalCards)

  override def displayGameEnded(scoreboard: Scoreboard, players: Players): Unit =
    println(s"Event received: Game ended. Final Scoreboard: $scoreboard")
    refreshScoreboardIfOpen(scoreboard, players)

    runOnUi:
      val homeButtonType = new ButtonType("Return to Home")

      val alert = new Alert(AlertType.Information):
        initOwner(stage)
        title = "Game Over"
        headerText = "The game has ended!"
        contentText = "Click the button below to return to the main menu."
        buttonTypes = Seq(homeButtonType)

      alert.showAndWait() match
        case Some(`homeButtonType`) =>
          println("Redirecting to home screen...")
          gameBoardDispatcher.stopListening()
          activeScoreboardStage.close()
          MainPage(stage)
        case _ =>
          println("Alert closed without action.")

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
    if rulesPanel != null then
      val isVisible = rulesPanel.isVisible
      rulesPanel.setVisible(!isVisible)
      rulesPanel.setManaged(!isVisible)
