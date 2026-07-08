package it.unibo.pps.wizard.application.gui.components

import it.unibo.pps.wizard.engine.model.basic.{Players, RoundRow}
import scalafx.scene.control.{TableColumn, TableView as FXTableView}
import scalafx.scene.layout.StackPane
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.StringProperty

class ScoreboardView(players: Players) extends StackPane:
  val table = new FXTableView[RoundRow]()
  table.fixedCellSize = 25.0

  table.style = """
      -fx-background-color: white;
      -fx-table-cell-border-color: #a0a0a0;
      -fx-font-size: 14px;
    """
  table.sortPolicy = _ => false

  private val colRound = new TableColumn[RoundRow, String]("Round"):
    prefWidth = 60
    style = "-fx-alignment: CENTER; -fx-font-weight: bold; -fx-background-color: #f0f0f0;"
    cellValueFactory = data => StringProperty(data.value.round.toString)
  table.columns.add(colRound)

  players.toList.foreach { player =>
    val playerGroupCol = new TableColumn[RoundRow, String](player.name.toString)

    val scoreCol = new TableColumn[RoundRow, String]("Punti"):
      prefWidth = 60
      style = "-fx-alignment: CENTER;"
      cellValueFactory = data => StringProperty(data.value.getScore(player.id))

    val bidCol = new TableColumn[RoundRow, String]("Prese"):
      prefWidth = 60
      style = "-fx-alignment: CENTER;"
      cellValueFactory = data => StringProperty(data.value.getBid(player.id))

    playerGroupCol.columns.addAll(scoreCol, bidCol)
    table.columns.add(playerGroupCol)
  }

  children.add(table)

  def updateData(rows: List[RoundRow], numPlayers: Int): Unit =
    table.items = ObservableBuffer(rows*)

    val exactWidth = 60.0 + (numPlayers * 120.0) + 5.0

    table.prefWidth = exactWidth
    table.minWidth = exactWidth
    table.maxWidth = exactWidth

    val headerHeight = 65.0
    val exactHeight = (rows.size * table.fixedCellSize.value) + headerHeight + 5.0

    table.prefHeight = exactHeight
    table.minHeight = exactHeight
    table.maxHeight = exactHeight
