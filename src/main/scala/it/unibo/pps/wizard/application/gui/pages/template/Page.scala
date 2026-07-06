package it.unibo.pps.wizard.application.gui.pages.template

import it.unibo.pps.wizard.application.gui.FXComponent
import scalafx.scene.Scene

trait Page extends FXComponent:
  def getScene: Scene
