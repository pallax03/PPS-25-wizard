package it.unibo.pps.wizard.application.scalafx.controllers

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.FXComponent
import scalafx.application.Platform
import scalafx.stage.Stage

abstract class Controller(protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends FXComponent:

  protected def runOnUi(action: => Unit): Unit =
    Platform.runLater(action)

  protected def changePage(pageFactory: Stage => Unit): Unit =
    runOnUi:
      pageFactory(stage)
