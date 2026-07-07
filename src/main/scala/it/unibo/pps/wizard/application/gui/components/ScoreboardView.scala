package it.unibo.pps.wizard.application.gui.components

import scalafx.scene.control.{TableColumn, TableView => FXTableView}
import scalafx.scene.layout.StackPane
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.StringProperty

class ScoreboardView(data: Seq[(String, String)]) extends StackPane:

  private val table = new FXTableView[(String, String)]():
    style = "-fx-background-color: white; -fx-table-cell-border-color: transparent; -fx-font-size: 16px;"

  private val colName = new TableColumn[(String, String), String]("Giocatore"):
    prefWidth = 200
    cellValueFactory = data => StringProperty(data.value._1)

  private val colScore = new TableColumn[(String, String), String]("Punti"):
    prefWidth = 100
    cellValueFactory = data => StringProperty(data.value._2)

  table.columns.addAll(colName, colScore)
  table.items = ObservableBuffer(data*)

  children.add(table)