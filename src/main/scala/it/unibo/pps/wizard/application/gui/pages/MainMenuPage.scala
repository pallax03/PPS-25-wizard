package it.unibo.pps.wizard.application.gui.pages

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.application.gui.controllers.MainPageController
import it.unibo.pps.wizard.application.gui.pages.template.{ApplicablePage, FXMLPage}
import scalafx.stage.Stage

case class MainMenuPage(override protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends FXMLPage(MainPageController(stage), "main-page")
    with ApplicablePage
