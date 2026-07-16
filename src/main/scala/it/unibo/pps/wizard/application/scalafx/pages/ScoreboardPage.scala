package it.unibo.pps.wizard.application.scalafx.pages

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.controllers.scoreboard.ScoreboardPageController
import it.unibo.pps.wizard.engine.model.basic.Players
import it.unibo.pps.wizard.engine.model.basic.RoundRow
import scalafx.stage.Stage

case class ScoreboardPage(override protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends Page(ScoreboardPageController(stage), "scoreboard-page"):

  def initializeTable(players: Players): Unit =
    controller.init(players)

  def updateData(rows: List[RoundRow], numPlayers: Int): Unit =
    controller.updateTableData(rows, numPlayers)
