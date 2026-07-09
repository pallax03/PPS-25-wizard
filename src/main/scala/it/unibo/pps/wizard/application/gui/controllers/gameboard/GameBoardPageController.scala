package it.unibo.pps.wizard.application.gui.controllers.gameboard

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.application.gui.components.*
import it.unibo.pps.wizard.application.gui.controllers.Controller
import it.unibo.pps.wizard.application.gui.managers.{HandManager, OpponentsManager, TableManager}
import it.unibo.pps.wizard.engine.adapters.WizardGameState.Running
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.basic.Card.*
import it.unibo.pps.wizard.engine.model.core.GameAction.PlayCard
import it.unibo.pps.wizard.engine.model.core.GameState.*
import it.unibo.pps.wizard.engine.model.core.{GameAction, GameState}
import javafx.animation.{ParallelTransition, ScaleTransition, TranslateTransition}
import javafx.scene.layout.{BorderPane, HBox, StackPane, VBox}
import scalafx.scene.Scene
import scalafx.stage.{Modality, Stage}
import scalafx.util.Duration

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class GameBoardPageController(stage: Stage)(using context: WizardApplicationContext)
    extends Controller(stage)
    with GameBoardView:

  @nowarn @FXML private var rootPane: BorderPane = _
  @nowarn @FXML private var tableContainer: HBox = _
  @nowarn @FXML private var handContainer: HBox = _
  @nowarn @FXML private var trumpContainer: VBox = _
  @nowarn @FXML private var currentPlayerContainer: VBox = _
  @nowarn @FXML private var playersContainer: HBox = _
  @nowarn @FXML private var gameInfoContainer: VBox = _
  @nowarn @FXML private var scoreboardContainer: StackPane = _

  @nowarn private var tableManager: TableManager = _
  @nowarn private var handManager: HandManager = _
  @nowarn private var opponentsManager: OpponentsManager = _
  @nowarn private var trumpView: TrumpView = _
  @nowarn private var currentPlayerView: HumanPlayerView = _
  @nowarn private var gameInfo: GameInfoView = _

  @FXML
  def initialize(): Unit =
    List(tableContainer, handContainer, trumpContainer, currentPlayerContainer, playersContainer)
      .foreach(_.getChildren.clear())
    this.initViewAndSubscribe()

  private def initViewAndSubscribe(): Unit =
    withRunningStatus:
      case status: Bidding =>
        runOnUi:
          buildUI(status)
          GameBoardEventDispatcher(this).startListening()
      case other =>
        println(s"Expected Bidding state but got: $other")

  private def buildUI(status: Bidding): Unit =
    this.tableManager = TableManager(this.tableContainer)
    this.tableManager.initializeTable(Table.empty, None)

    val currentPlayerId = status.core.players.toList.head.id
    val playerHand = status.core.hands.getHand(currentPlayerId).getOrElse(Hand.empty)

    this.handManager = HandManager(
      this.handContainer,
      onCardDragged =
        (mouseX, mouseY) => tableManager.setHighlight(tableManager.isOver(mouseX, mouseY)),
      onCardDropped = (card, mouseX, mouseY) =>
        tableManager.setHighlight(false)
        if tableManager.isOver(mouseX, mouseY) then
          context.inboundPort.submitAction(PlayCard(currentPlayerId, card))
    )
    this.handManager.updateHand(playerHand)

    this.trumpView = new TrumpView(status.core.trump)
    this.trumpContainer.getChildren.add(this.trumpView.delegate)

    val currentPlayer = status.core.players.toList.head
    val isMyTurn = status.currentPlayer == currentPlayer.id
    this.currentPlayerView = HumanPlayerView(
      currentPlayer,
      isMyTurn,
      onBidSubmitted =
        bid => context.inboundPort.submitAction(GameAction.PlaceBid(currentPlayer.id, bid)),
      onTrumpSelected = color =>
        context.inboundPort.submitAction(
          GameAction.ResolveTrumpColor(currentPlayer.id, color)
        )
    )
    this.currentPlayerContainer.getChildren.add(this.currentPlayerView.delegate)

    val allOtherPlayers = status.core.players.filter(_.id != currentPlayerId)
    this.opponentsManager = OpponentsManager(playersContainer)
    this.opponentsManager.renderAllOpponents(allOtherPlayers)

    this.gameInfo = GameInfoView(status.core.round.value, status.getClass.getSimpleName)
    this.gameInfoContainer.getChildren.add(this.gameInfo.delegate)

  override def displayWaitingForTrump(value: PlayerId): Unit =
    println(s"Event received: Waiting for Trump selection from player $value")
    if value == currentPlayerView.player.id then currentPlayerView.setTrumpSelectionEnabled(true)
    else currentPlayerView.setTrumpSelectionEnabled(false)

  override def displayTrickWon(winnerId: PlayerId, trickedCards: List[Card]): Unit =
    println(s"Event received: Trick won by player $winnerId with cards: $trickedCards")
    tableManager.initializeTable(Table.empty, None)
    onTurnChanged(winnerId)

  override def displayPhaseChanged(phase: String): Unit =
    this.gameInfo.changePhase(phase)
    if phase == "Bidding" then currentPlayerView.setBidTextFieldEnabled(true)
    else currentPlayerView.setBidTextFieldEnabled(false)

  override def displayCardsDealt(
      playerId: PlayerId,
      hands: Hands,
      trump: Trump,
      round: Round
  ): Unit =
    this.gameInfo.incrementRound(round.value)
    println(s"Event received: Cards dealt. Player: $playerId Trump: $trump Hands: $hands")
    this.trumpView = TrumpView(trump)
    val currentPlayerId = this.currentPlayerView.player.id
    val newHand = hands.getHand(currentPlayerId).getOrElse(Hand.empty)
    this.handManager.updateHand(newHand)
    onTurnChanged(playerId)

  override def displayCardPlayed(playerId: PlayerId, card: Card): Unit =
    println(s"Event received: Card played by player $playerId: $card")
    tableManager.addCard(card, playerId, false)
    handManager.removeCard(card)
    applyCurrentTurn()

  override def displayTrumpSelected(playerId: PlayerId, color: Card.Color): Unit =
    println(s"Event received: Trump selected by player $playerId: $color")
    trumpView.updateTrumpColor(color)

  override def displayBidPlaced(playerId: PlayerId, bid: Bid): Unit =
    println(s"Event received: Bid placed by player $playerId: $bid")
    if playerId == currentPlayerView.player.id then this.currentPlayerView.updateBid(bid)
    else this.opponentsManager.updateOpponentBid(playerId, bid)
    applyCurrentTurn()

  private def onTurnChanged(nextPlayerId: PlayerId): Unit =
    val isMyTurn = nextPlayerId == currentPlayerView.player.id
    this.currentPlayerView.setTurnActive(isMyTurn)
    this.opponentsManager.updateActiveTurn(nextPlayerId)

  private def applyCurrentTurn(): Unit =
    withRunningStatus:
      case status: (Bidding | Playing) =>
        val nextPlayerId = status match
          case b: Bidding => b.currentPlayer
          case p: Playing => p.currentPlayerTurn

        onTurnChanged(nextPlayerId)
      case other =>
        println(s"Expected Bidding or Playing state but got: $other")

  private def withRunningStatus(action: GameState => Unit): Unit =
    context.inboundPort.getState.onComplete:
      case Success(Running(status)) => action(status)
      case Success(otherState) =>
        println(s"Game is not in a valid state for the match: $otherState")
      case Failure(exception) =>
        println(s"Error occurred while retrieving the initial state: ${exception.getMessage}")

  @FXML
  def openScoreboardWindow(): Unit =
    withRunningStatus:
      case status: (Bidding | Playing) =>
        val core = status match
          case b: Bidding => b.core
          case p: Playing => p.core

        runOnUi:
          val scoresMap = core.scoreboard
          val allPlayers = core.players

          val scoreboardView = new ScoreboardView(allPlayers)

          val initialRows = RoundRow.createRows(allPlayers, scoresMap)
          scoreboardView.updateData(initialRows, allPlayers.toList.size)

          val scoreboardStage = new Stage():
            initModality(Modality.ApplicationModal)
            title = "Scoreboard"
            scene = new Scene(scoreboardView)
            resizable = false

          scoreboardStage.sizeToScene()
          scoreboardStage.show()
      case other =>
        println(s"Expected Bidding state but got: $other")

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
