package it.unibo.pps.wizard.application.scalafx.controllers

import it.unibo.pps.wizard.application.scalafx.FXComponent
import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import scalafx.application.Platform
import scalafx.stage.Stage

/**
 * Represents a controller in the application, which is responsible for handling user interactions and managing the state of the application.
 *
 * @param stage the primary stage of the application
 * @param context the application context providing access to various components
 */
abstract class Controller(protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends FXComponent:

  protected def runOnUi(action: => Unit): Unit =
    Platform.runLater(action)

  protected def changePage(pageFactory: Stage => Unit): Unit =
    runOnUi:
      pageFactory(stage)
