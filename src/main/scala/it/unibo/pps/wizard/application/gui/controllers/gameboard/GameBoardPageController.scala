package it.unibo.pps.wizard.application.gui.controllers.gameboard

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.application.gui.components.*
import it.unibo.pps.wizard.application.gui.controllers.Controller
import it.unibo.pps.wizard.application.gui.managers.{HandManager, OpponentsManager, TableManager, TrumpManager}
import it.unibo.pps.wizard.application.gui.pages.ScoreboardPage
import it.unibo.pps.wizard.engine.adapters.WizardGameState.Running
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.basic.Card.*
import it.unibo.pps.wizard.engine.model.core.GameAction.PlayCard
import it.unibo.pps.wizard.engine.model.core.GameState.*
import it.unibo.pps.wizard.engine.model.core.{GameAction, GameState}
import javafx.animation.{ParallelTransition, ScaleTransition, TranslateTransition}
import javafx.scene.layout.{HBox, StackPane, VBox}
import scalafx.stage.{Modality, Stage}
import scalafx.util.Duration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class GameBoardPageController(stage: Stage, currentPlayerId: PlayerId)(using context: WizardApplicationContext)
    extends Controller(stage)
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
  @nowarn private var activeScoreboardStage: Option[Stage] = _
  private var activeScoreboardPage: Option[ScoreboardPage] = None

  @FXML
  def initialize(): Unit =
    println("Initializing GameBoardPageController...")
    GameBoardEventDispatcher(this).startListening()

    runOnUi:
      List(tableContainer, handContainer, trumpContainer, currentPlayerContainer, playersContainer)
        .foreach(_.getChildren.clear())
      buildUI()

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

  override def displayWaitingForTrump(value: PlayerId): Unit =
    println(s"Event received: Waiting for Trump selection from player $value")
    if value == currentPlayerView.player.id then currentPlayerView.setTrumpSelectionEnabled(true)
    else currentPlayerView.setTrumpSelectionEnabled(false)

  override def displayTrickWon(winnerId: PlayerId, trickedCards: List[Card]): Unit =
    println(s"Event received: Trick won by player $winnerId with cards: $trickedCards")
    tableManager.initializeTable(Table.empty, None)
    this.currentPlayerView.resetBid()
    this.opponentsManager.resetOpponentsBid()

  override def displayPhaseChanged(phase: String): Unit =
    this.gameInfo.changePhase(phase)
    this.currentPlayerView.setBidTextFieldEnabled(phase == "Bidding")

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
    if playerId == currentPlayerId then this.currentPlayerView.updateBid(bid)
    else this.opponentsManager.updateOpponentBid(playerId, bid)

  override def displayTurnChanged(nextPlayerId: PlayerId, phase: String): Unit =
    val isMyTurn = nextPlayerId == currentPlayerId
    this.currentPlayerView.setTurnActive(isMyTurn, phase)
    this.opponentsManager.updateActiveTurn(nextPlayerId, phase)

  override def displayRoundScored(scoreboard: Scoreboard): Unit =
    println(s"Event received: Round scored. Scoreboard: $scoreboard")
    refreshScoreboardIfOpen()

  private def withRunningStatus(action: GameState => Unit): Unit =
    context.inboundPort.getState.onComplete:
      case Success(Running(status)) => action(status)
      case Success(otherState) =>
        println(s"Game is not in a valid state for the match: $otherState")
      case Failure(exception) =>
        println(s"Error occurred while retrieving the initial state: ${exception.getMessage}")

  @FXML
  def openScoreboardWindow(): Unit =
    activeScoreboardStage match
      case Some(stg) if stg.isShowing =>
        stg.toFront()

      case _ =>
        withScoreboardData: (allPlayers, rows) =>
          val scoreboardStage = new Stage():
            initModality(Modality.None)
            title = "Scoreboard"
            resizable = false
            onCloseRequest = _ =>
              activeScoreboardStage = None
              activeScoreboardPage = None

          val scoreboardPage = ScoreboardPage(scoreboardStage)

          scoreboardPage.initializeTable(allPlayers)
          scoreboardPage.updateData(rows, allPlayers.toList.size)

          scoreboardStage.show()

          activeScoreboardStage = Some(scoreboardStage)
          activeScoreboardPage = Some(scoreboardPage)

  private def refreshScoreboardIfOpen(): Unit =
    activeScoreboardPage.foreach: page =>
      withScoreboardData: (allPlayers, rows) =>
        page.updateData(rows, allPlayers.toList.size)

  private def withScoreboardData(action: (Players, List[RoundRow]) => Unit): Unit =
    withRunningStatus:
      case status: (Bidding | Playing) =>
        val core = status match
          case b: Bidding => b.core
          case p: Playing => p.core

        runOnUi:
          val allPlayers = core.players
          val rows = RoundRow.createRows(allPlayers, core.scoreboard)
          action(allPlayers, rows)

      case other =>
        println(s"Scoreboard action skipped: Game is not in a valid state ($other).")

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
