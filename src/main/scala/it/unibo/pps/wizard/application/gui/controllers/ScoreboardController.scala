package it.unibo.pps.wizard.application.gui.controllers

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.application.gui.controllers.template.FXMLController
import scalafx.scene.control.{TableColumn, TableView as FXTableView}
import scalafx.collections.ObservableBuffer
import scalafx.beans.property.StringProperty
import scalafx.stage.Stage

import scala.annotation.nowarn

class ScoreboardController(override protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends FXMLController:

  @nowarn @FXML private var scoreboardTable: FXTableView[(String, String)] = _
  @nowarn @FXML private var colName: TableColumn[(String, String), String] = _
  @nowarn @FXML private var colScore: TableColumn[(String, String), String] = _

  @FXML
  def initialize(): Unit =
    colName.cellValueFactory = data => StringProperty(data.value._1)
    colScore.cellValueFactory = data => StringProperty(data.value._2)

  def setData(data: Seq[(String, String)]): Unit =
    scoreboardTable.items = ObservableBuffer(data*)