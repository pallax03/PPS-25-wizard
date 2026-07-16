package it.unibo.pps.wizard.application.scalafx.controllers.scoreboard

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.controllers.Controller
import it.unibo.pps.wizard.engine.model.basic.Players
import it.unibo.pps.wizard.engine.model.basic.RoundRow
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.control.{TableColumn => FXTableColumn}
import javafx.scene.control.{TableView => FXTableView}
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.stage.Stage

import scala.annotation.nowarn
import scala.jdk.CollectionConverters._

class ScoreboardPageController(stage: Stage)(using context: WizardApplicationContext)
    extends Controller(stage):

  @nowarn @FXML private var scoreboardTable: FXTableView[RoundRow] = _
  @nowarn @FXML private var colRound: FXTableColumn[RoundRow, String] = _

  private var isStructureInitialized = false

  def init(players: Players): Unit =
    if (!isStructureInitialized) {
      scoreboardTable.setSelectionModel(null)
      scoreboardTable.setSortPolicy(_ => false)
      scoreboardTable.setFocusTraversable(false)

      colRound.setCellValueFactory(data => StringProperty(data.getValue.round.toString))

      players.toList.foreach { player =>
        val playerGroupCol = new FXTableColumn[RoundRow, String](player.name.toString)
        playerGroupCol.setResizable(false)
        playerGroupCol.setSortable(false)

        val scoreCol = new FXTableColumn[RoundRow, String]("Points")
        scoreCol.setPrefWidth(60.0)
        scoreCol.setStyle("-fx-alignment: CENTER;")
        scoreCol.setCellValueFactory(data => StringProperty(data.getValue.getScore(player.id)))
        scoreCol.setResizable(false)
        scoreCol.setSortable(false)

        val bidCol = new FXTableColumn[RoundRow, String]("Bids")
        bidCol.setPrefWidth(60.0)
        bidCol.setStyle("-fx-alignment: CENTER;")
        bidCol.setCellValueFactory(data => StringProperty(data.getValue.getBid(player.id)))
        bidCol.setResizable(false)
        bidCol.setSortable(false)

        playerGroupCol.getColumns.addAll(scoreCol, bidCol)

        lockColumnOrder(playerGroupCol.getColumns)
        scoreboardTable.getColumns.add(playerGroupCol)
      }

      lockColumnOrder(scoreboardTable.getColumns)
      isStructureInitialized = true
    }
    val rows = RoundRow.initRows(players)
    updateTableData(rows, players.toList.size)

  def updateTableData(rows: List[RoundRow], numPlayers: Int): Unit =
    scoreboardTable.setItems(ObservableBuffer(rows*).delegate)

    val exactWidth = 60.0 + (numPlayers * 120.0) + 5.0
    scoreboardTable.setPrefWidth(exactWidth)
    scoreboardTable.setMinWidth(exactWidth)
    scoreboardTable.setMaxWidth(exactWidth)

    val headerHeight = 65.0
    val exactHeight = (rows.size * scoreboardTable.getFixedCellSize) + headerHeight + 5.0
    scoreboardTable.setPrefHeight(exactHeight)
    scoreboardTable.setMinHeight(exactHeight)
    scoreboardTable.setMaxHeight(exactHeight)

    scoreboardTable.refresh()

  private def lockColumnOrder(
      columnsList: ObservableList[FXTableColumn[RoundRow, ?]]
  ): Unit =
    val permanentOrder: List[FXTableColumn[RoundRow, ?]] = columnsList.asScala.toList
    columnsList.addListener(new ListChangeListener[FXTableColumn[RoundRow, ?]] {
      private var updating = false
      override def onChanged(
          c: ListChangeListener.Change[? <: FXTableColumn[RoundRow, ?]]
      ): Unit =
        if (!updating) {
          updating = true
          columnsList.setAll(permanentOrder.asJavaCollection)
          updating = false
        }
    })
