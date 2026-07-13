package it.unibo.pps.wizard.application.scalafx.pages

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.controllers.gameboard.GameBoardPageController
import it.unibo.pps.wizard.engine.model.basic.PlayerId
import scalafx.stage.Stage

case class GameBoardPage(override protected val stage: Stage, protected val currentPlayerId: PlayerId)(using
    protected val context: WizardApplicationContext
) extends Page(GameBoardPageController(stage, currentPlayerId), "game-board-page")
