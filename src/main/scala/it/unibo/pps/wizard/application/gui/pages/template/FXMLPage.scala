package it.unibo.pps.wizard.application.gui.pages.template

import it.unibo.pps.wizard.application.gui.controllers.template.FXMLController
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import scalafx.scene.Scene

abstract class FXMLPage[C <: FXMLController](
    protected val controller: C,
    protected val fxmlPath: String
) extends Page:
  private val fxmlLoader: FXMLLoader = FXMLPage.createFXMLLoader(this.fxmlPath)
  this.fxmlLoader.setController(this.controller)

  override def getScene: Scene = Scene(this.fxmlLoader.load[Parent])

object FXMLPage:

  private def createFXMLLoader(fxmlPath: String): FXMLLoader =
    FXMLLoader(
      Option(this.getClass.getResource(s"/fxml/pages/$fxmlPath.fxml")).getOrElse(
        throw new IllegalArgumentException(
          s"Could not load the specified resource: $fxmlPath. Try with a different path."
        )
      )
    )
