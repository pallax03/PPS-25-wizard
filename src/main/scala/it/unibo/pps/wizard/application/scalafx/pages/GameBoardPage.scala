package it.unibo.pps.wizard.application.scalafx.pages

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.controllers.gameboard.GameBoardPageController
import it.unibo.pps.wizard.engine.model.basic.PlayerId
import scalafx.stage.Stage

/**
 * Represents the game board page of the application, which is associated with the GameBoardPageController and the "game-board-page" FXML file.
 *
 * @param stage the primary stage of the application
 * @param currentPlayerId the ID of the current player
 * @param context the application context providing access to various components
 */
case class GameBoardPage(
    override protected val stage: Stage,
    protected val currentPlayerId: PlayerId
)(using
    protected val context: WizardApplicationContext
) extends Page(GameBoardPageController(stage, currentPlayerId), "game-board-page")
