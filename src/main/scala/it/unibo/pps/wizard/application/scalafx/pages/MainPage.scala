package it.unibo.pps.wizard.application.scalafx.pages

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.controllers.mainpage.MainPageController
import scalafx.stage.Stage

/**
 * Represents the main page of the application, which is associated with the MainPageController and the "main-page" FXML file.
 *
 * @param stage the primary stage of the application
 * @param context the application context providing access to various components
 */
case class MainPage(override protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends Page(MainPageController(stage), "main-page")
