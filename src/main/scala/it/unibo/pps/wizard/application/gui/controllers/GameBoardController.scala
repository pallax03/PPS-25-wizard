package it.unibo.pps.wizard.application.gui.controllers

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.application.gui.controllers.template.FXMLController
import it.unibo.pps.wizard.application.gui.components.{GameInfo, HumanPlayerView, OpponentsView, ScoreboardView, TrumpView}
import it.unibo.pps.wizard.application.gui.managers.{HandManager, TableManager}
import it.unibo.pps.wizard.engine.events.{ActionEvent, ProgressEvent}
import it.unibo.pps.wizard.engine.model.game.WizardGameState.Running
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.basic.Card.*
import it.unibo.pps.wizard.engine.model.core.GameAction.PlayCard
import it.unibo.pps.wizard.engine.model.core.{GameAction, GameState}
import javafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.application.Platform
import scalafx.stage.{Modality, Stage}
import it.unibo.pps.wizard.engine.model.core.GameState.*
import javafx.animation.{ParallelTransition, ScaleTransition, TranslateTransition}
import scalafx.scene.control.Label
import scalafx.scene.Scene
import javafx.scene.layout.StackPane
import scalafx.util.Duration

import scala.annotation.nowarn
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class GameBoardController(override protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends FXMLController:

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
  @nowarn private var trumpView: TrumpView = _
  @nowarn private var currentPlayerView: HumanPlayerView = _
  @nowarn private var opponentsView: OpponentsView = _
  @nowarn private var gameInfo: Label = _

  @FXML
  def initialize(): Unit =
    println("GameBoardController istanziato")
    List(tableContainer, handContainer, trumpContainer, currentPlayerContainer, playersContainer)
      .foreach(_.getChildren.clear())
    this.initViewAndSubscribe()

  private def initViewAndSubscribe(): Unit =
    println("Richiesta dello stato iniziale del gioco al proxy...")

    context.wizardEngineProxy.getState.onComplete:
      case Success(Running(status: Bidding)) =>
        Platform.runLater:
          println("Stato di gioco ricevuto. Generazione componenti e iscrizione eventi...")

          buildUI(status)
          subscribeToEvents()

      case Success(otherState) =>
        println(s"Il gioco non è in uno stato valido per la partita: $otherState")

      case Failure(exception) =>
        println(s"Errore nel recupero dello stato iniziale: ${exception.getMessage}")

  private def buildUI(status: Bidding): Unit =
    this.tableManager = TableManager(this.tableContainer)
    this.tableManager.initializeTable(Table.empty, None)

    val currentPlayerId = status.core.players.toList.head.id
    val playerHand = status.core.hands.getHand(currentPlayerId).getOrElse(Hand.empty)

    this.handManager = HandManager(this.handContainer)
    this.handManager.initializeHand(
      playerHand,
      onCardDragged =
        (mouseX, mouseY) => tableManager.setHighlight(tableManager.isOver(mouseX, mouseY)),
      onCardDropped = (card, mouseX, mouseY) => {
        tableManager.setHighlight(false)
        if tableManager.isOver(mouseX, mouseY) then
          context.wizardEngineProxy.submitAction(PlayCard(currentPlayerId, card))
      }
    )

    this.trumpView = new TrumpView(status.trump)
    this.trumpContainer.getChildren.add(this.trumpView.delegate)

    val currentPlayer = status.core.players.toList.head
    val isMyTurn = status.currentPlayer == currentPlayer.id
    this.currentPlayerView = HumanPlayerView(
      currentPlayer,
      isMyTurn,
      onBidSubmitted =
        bid => context.wizardEngineProxy.submitAction(GameAction.PlaceBid(currentPlayer.id, bid)),
      onTrumpSelected = color =>
        context.wizardEngineProxy.submitAction(GameAction.ChooseTrump(currentPlayer.id, color))
    )
    this.currentPlayerContainer.getChildren.add(this.currentPlayerView.delegate)

    val allOtherPlayers = status.core.players.filter(_.id != currentPlayerId)
    this.opponentsView = OpponentsView(playersContainer)
    this.opponentsView.renderAllOpponents(allOtherPlayers, currentPlayerId)

    this.gameInfo = GameInfo(status.core.round.value, status.getClass.getSimpleName)
    this.gameInfoContainer.getChildren.add(this.gameInfo)

  private def subscribeToEvents(): Unit =
    println("Subscribed to game events")
    context.wizardEngineProxy.subscribe[ActionEvent]:
      case ActionEvent.CardPlayed(playerId, card)     => onCardPlayed(playerId, card)
      case ActionEvent.TrumpSelected(playerId, color) => onTrumpSelected(playerId, color)
      case ActionEvent.BidPlaced(playerId, bid)       => onBidPlaced(playerId, bid)

    context.wizardEngineProxy.subscribe[ProgressEvent]:
      case ProgressEvent.CardsDealt(hands, trump, round) => onCardsDealt(hands, trump, round)
      case ProgressEvent.TrickWon(winnerId, trickedCards) => onTrickWon(winnerId, trickedCards)
      case ProgressEvent.RoundScored(scoreboard) => ???
//          Platform.runLater:
//          println(s"Evento ricevuto: Round completato. Classifica aggiornata: $scoreboard")
      case ProgressEvent.PhaseChanged(phase) => onPhaseChanged(phase)

  private def onTrickWon(winnerId: PlayerId, trickedCards: List[Card]): Unit =
    Platform.runLater:
      println(s"Evento ricevuto: Trick vinto dal giocatore $winnerId con le carte: $trickedCards")
      tableManager.initializeTable(Table.empty, None)

  private def onPhaseChanged(phase: String): Unit =
    Platform.runLater:
      GameInfo.changePhase(this.gameInfo, phase)

  private def onCardsDealt(hands: Hands, trump: Trump, round: Round) =
    Platform.runLater:
      GameInfo.incrementRound(this.gameInfo, round.value)
      println(s"Evento ricevuto: Carte distribuite. Trump: $trump Hands: ${hands}")
//      println(s"Evento ricevuto: Carte distribuite. Trump: $trump")
//      handManager.updateHand(hands.getHand(currentPlayerView.player.id).getOrElse(Hand.empty))
//      trumpView.updateTrumpColor(trump.color)

  private def onCardPlayed(playerId: PlayerId, card: Card): Unit =
    Platform.runLater:
      println(s"Evento ricevuto: Carta giocata dal giocatore $playerId: $card")
      tableManager.addCard(card, playerId, false)
      handManager.removeCard(card)

  private def onTrumpSelected(playerId: PlayerId, color: Card.Color): Unit =
    Platform.runLater:
      println(s"Evento ricevuto: Trump selezionato dal giocatore $playerId: $color")
      trumpView.updateTrumpColor(color)

  private def onBidPlaced(playerId: PlayerId, bid: Bid): Unit =
    Platform.runLater:
      println(s"Evento ricevuto: Offerta piazzata dal giocatore $playerId: $bid")
      if playerId == currentPlayerView.player.id then
        this.currentPlayerView.updateBid(bid)
      else
        this.opponentsView.updateOpponentBid(playerId, bid)

  @FXML
  def openScoreboardWindow(): Unit =
    context.wizardEngineProxy.getState.onComplete:
      case Success(Running(status: (Bidding | Playing))) =>
        val core = status match
          case b: Bidding => b.core
          case p: Playing => p.core
          
        Platform.runLater:
          val scoresMap = core.scoreboard
          val allPlayers = core.players

          val scoreboardView = new ScoreboardView(allPlayers)

          val initialRows = RoundRow.createRows(allPlayers, scoresMap)
          scoreboardView.updateData(initialRows, allPlayers.toList.size)

          val scoreboardStage = new Stage():
            initModality(Modality.ApplicationModal)
            title = "Classifica Round per Round"
            scene = new Scene(scoreboardView)
            resizable = false

          scoreboardStage.sizeToScene()
          scoreboardStage.show()
      case Success(otherState) =>
        println(s"Il gioco non è in uno stato valido per la partita: $otherState")

      case Failure(exception) =>
        println(s"Errore nel recupero dello stato iniziale: ${exception.getMessage}")

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
