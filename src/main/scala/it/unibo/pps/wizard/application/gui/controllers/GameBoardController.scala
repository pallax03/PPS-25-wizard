package it.unibo.pps.wizard.application.gui.controllers

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.application.gui.controllers.template.FXMLController
import it.unibo.pps.wizard.application.gui.components.{BasePlayerView, BotPlayerView, HumanPlayerView, TrumpView}
import it.unibo.pps.wizard.application.gui.managers.{HandManager, TableManager}
import it.unibo.pps.wizard.engine.events.ActionEvent
import it.unibo.pps.wizard.engine.model.game.WizardGameState.Running
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.basic.Card.*
import it.unibo.pps.wizard.engine.model.core.GameAction.PlayCard
import it.unibo.pps.wizard.engine.model.core.{GameAction, GameState}
import javafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.application.Platform
import scalafx.stage.Stage
import it.unibo.pps.wizard.engine.model.core.GameState.*
import scalafx.scene.control.Label
import scalafx.scene.text.{Font, FontWeight}

import scala.annotation.nowarn
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class GameBoardController(override protected val stage: Stage)(using
                                                               protected val context: WizardApplicationContext
) extends FXMLController:

  // --- Nodi iniettati da FXML ---
  @nowarn @FXML private var rootPane: BorderPane = _
  @nowarn @FXML private var tableContainer: HBox = _
  @nowarn @FXML private var handContainer: HBox = _
  @nowarn @FXML private var trumpContainer: VBox = _
  @nowarn @FXML private var currentPlayerContainer: VBox = _
  @nowarn @FXML private var playersContainer: HBox = _
  @nowarn @FXML private var gameInfoContainer: VBox = _

  @nowarn private var tableManager: TableManager = _
  @nowarn private var handManager: HandManager = _
  @nowarn private var trumpView: TrumpView = _
  @nowarn private var currentPlayerView: HumanPlayerView = _

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
    this.handManager.initializeHand(playerHand,
        onCardDragged = (mouseX, mouseY) => tableManager.setHighlight(tableManager.isOver(mouseX, mouseY)),
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
      onBidSubmitted = bid => context.wizardEngineProxy.submitAction(GameAction.PlaceBid(currentPlayer.id, bid)),
      onTrumpSelected = color => context.wizardEngineProxy.submitAction(GameAction.ChooseTrump(currentPlayer.id, color))
    )
    this.currentPlayerContainer.getChildren.add(this.currentPlayerView.delegate)

    val allOtherPlayers = status.core.players.filter(_.id != currentPlayerId)
    allOtherPlayers.toList.foreach: player =>
      val isHisTurn = status.currentPlayer == player.id
      val botView = BotPlayerView(player, isHisTurn)
      this.playersContainer.getChildren.add(botView.delegate)

    val gameInfo = new Label(s"Round: ${status.getClass.getSimpleName}"):
      font = Font.font("Arial", FontWeight.Normal, 25)
      textFill = scalafx.scene.paint.Color.White
    this.gameInfoContainer.getChildren.add(gameInfo)

  private def subscribeToEvents(): Unit =
    println("Subscribed to game events")
    context.wizardEngineProxy.subscribe[ActionEvent]:
      case ActionEvent.CardPlayed(playerId, card) => onCardPlayed(playerId, card)
      case ActionEvent.TrumpSelected(playerId, color) => onTrumpSelected(playerId, color)
      case ActionEvent.BidPlaced(playerId, bid) => onBidPlaced(playerId, bid)

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
      playersContainer.getChildren.forEach: playerViewNode =>
        val playerView = playerViewNode.asInstanceOf[BasePlayerView]
        if playerView.player.id == playerId then
          playerView.updateBid(bid)