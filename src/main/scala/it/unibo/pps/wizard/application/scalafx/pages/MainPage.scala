package it.unibo.pps.wizard.application.scalafx.pages

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.controllers.mainpage.MainPageController
import scalafx.stage.Stage

case class MainPage(override protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends Page(MainPageController(stage), "main-page")
