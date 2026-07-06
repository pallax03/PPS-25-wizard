package it.unibo.pps.wizard.application.gui.controllers

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.application.gui.controllers.template.FXMLController
import it.unibo.pps.wizard.application.gui.components.{HandView, PlayerView, TableView, TrumpView}
import it.unibo.pps.wizard.engine.model.game.WizardGameState.Running
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameState
import javafx.scene.layout.{BorderPane, HBox, StackPane}
import scalafx.application.Platform
import scalafx.stage.Stage
import it.unibo.pps.wizard.engine.model.core.GameState.*

import scala.annotation.nowarn
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class GameBoardController(override protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends FXMLController:

  @nowarn @FXML private var rootPane: BorderPane = _
  @nowarn @FXML private var tableContainer: StackPane = _
  @nowarn @FXML private var handContainer: StackPane = _
  @nowarn @FXML private var trumpContainer: StackPane = _
  @nowarn @FXML private var playersContainer: HBox = _

  @nowarn private var tableView: TableView = _
  @nowarn private var handView: HandView = _
  @nowarn private var trumpView: TrumpView = _

  @FXML
  def initialize(): Unit =
    println("GameBoardController istanziato")
    this.subscribeToEvents()
    this.initView()

  private def subscribeToEvents(): Unit =
    println("Subscribed to game events")

  private def initView(): Unit =
    println("Richiesta dello stato iniziale del gioco al proxy...")

    context.wizardEngineProxy.getState.onComplete:
      case Success(Running(status: Bidding)) =>
        Platform.runLater:
          println("Stato di gioco ricevuto con successo. Generazione dei componenti grafici...")

          val playerHand: Hand =
            status.core.hands.getHand(status.core.players.toList.head.id).getOrElse(Hand.empty)
          val currentTable: Table = Table.empty
          val trump: Trump = status.trump

          val allPlayers: Players = status.core.players

          this.tableView = new TableView(currentTable)

          this.handView = new HandView(
            playerHand,
            onCardDragged =
              (mouseX, mouseY) => tableView.setHighlight(tableView.isOver(mouseX, mouseY)),
            onCardDropped = (card, mouseX, mouseY) =>
              tableView.setHighlight(false)
              if tableView.isOver(mouseX, mouseY) then
                println(s"Carta giocata dal giocatore: $card")
            // Esempio di interazione esagonale:
            // context.wizardEngineProxy.submitAction(...)
          )

          this.trumpView = new TrumpView(trump)

          tableContainer.getChildren.clear()
          handContainer.getChildren.clear()
          trumpContainer.getChildren.clear()
          playersContainer.getChildren.clear()

          allPlayers.toList.foreach: player =>
            val isHisTurn = status.currentPlayer == player.id
            val playerView = new PlayerView(player, isCurrentTurn = isHisTurn)
            playersContainer.getChildren.add(playerView.delegate)

          tableContainer.getChildren.add(this.tableView.delegate)
          handContainer.getChildren.add(this.handView.delegate)
          trumpContainer.getChildren.add(this.trumpView.delegate)

      case Success(otherState) =>
        println(s"Il gioco non è in uno stato valido per la partita: $otherState")

      case Failure(exception) =>
        println(s"Errore nel recupero dello stato iniziale: ${exception.getMessage}")
