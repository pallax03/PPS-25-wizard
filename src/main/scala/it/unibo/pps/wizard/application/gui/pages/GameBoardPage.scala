package it.unibo.pps.wizard.application.gui.pages

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.application.gui.controllers.GameBoardPageController
import scalafx.stage.Stage

case class GameBoardPage(override protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends Page(GameBoardPageController(stage), "game-board-page")
