package it.unibo.pps.wizard.application.gui.pages

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.application.gui.controllers.mainpage.MainPageController
import scalafx.stage.Stage

case class MainPage(override protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends Page(MainPageController(stage), "main-page")
