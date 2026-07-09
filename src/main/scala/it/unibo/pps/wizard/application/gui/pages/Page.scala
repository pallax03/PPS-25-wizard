package it.unibo.pps.wizard.application.gui.pages

import it.unibo.pps.wizard.application.gui.FXComponent
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import scalafx.scene.Scene
import scalafx.stage.Stage

abstract class Page[C](
    protected val controller: C,
    protected val fxmlPath: String
) extends FXComponent:
  protected def stage: Stage

  private val fxmlLoader: FXMLLoader = createFXMLLoader(this.fxmlPath)
  this.fxmlLoader.setController(this.controller)

  stage.scene = Scene(this.fxmlLoader.load[Parent])
  stage.sizeToScene()
  stage.centerOnScreen()

  private def createFXMLLoader(fxmlPath: String): FXMLLoader =
    FXMLLoader(
      Option(this.getClass.getResource(s"/fxml/pages/$fxmlPath.fxml")).getOrElse(
        throw new IllegalArgumentException(
          s"Could not load the specified resource: $fxmlPath. Try with a different path."
        )
      )
    )
