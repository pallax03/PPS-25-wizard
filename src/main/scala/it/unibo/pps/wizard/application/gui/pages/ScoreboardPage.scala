package it.unibo.pps.wizard.application.gui.pages

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.application.gui.controllers.scoreboard.ScoreboardPageController
import it.unibo.pps.wizard.engine.model.basic.{Players, RoundRow}
import scalafx.stage.Stage

case class ScoreboardPage(override protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends Page(ScoreboardPageController(stage), "scoreboard-page"):

  def initializeTable(players: Players): Unit =
    controller.init(players)
  
  def updateData(rows: List[RoundRow], numPlayers: Int): Unit =
    controller.updateTableData(rows, numPlayers)
