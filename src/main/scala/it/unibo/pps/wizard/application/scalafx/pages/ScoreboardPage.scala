package it.unibo.pps.wizard.application.scalafx.pages

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.controllers.scoreboard.ScoreboardPageController
import it.unibo.pps.wizard.engine.model.basic.Players
import it.unibo.pps.wizard.engine.model.basic.RoundRow
import scalafx.stage.Stage

/**
 * Represents the score board page for the current game, which is associated with the ScoreboardPageController and the "score-board-page" FXML file.
 *
 * @param stage new stage created by GameBoardPageController
 * @param context the application context providing access to various components
 */
case class ScoreboardPage(override protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends Page(ScoreboardPageController(stage), "scoreboard-page"):

  def initializeTable(players: Players): Unit =
    controller.init(players)

  def updateData(rows: List[RoundRow], numPlayers: Int): Unit =
    controller.updateTableData(rows, numPlayers)
