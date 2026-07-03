package it.unibo.pps.wizard.application.gui.pages.template

import it.unibo.pps.wizard.application.gui.StageComponent
import it.unibo.pps.wizard.application.gui.pages.template.Page

trait ApplicablePage extends Page with StageComponent:
  this.stage.scene = this.getScene
  this.stage.sizeToScene()
  this.stage.centerOnScreen()
