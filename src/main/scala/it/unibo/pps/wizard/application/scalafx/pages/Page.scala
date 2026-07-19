package it.unibo.pps.wizard.application.scalafx.pages

import it.unibo.pps.wizard.application.scalafx.FXComponent
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import scalafx.scene.Scene
import scalafx.stage.Stage

/**
 * Represents a page in the application, which is associated with a controller and an FXML file.
 *
 * @param controller the controller associated with the page
 * @param fxmlPath the path to the FXML file for the page
 * @tparam C the type of the controller
 */
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

  /**
   * Creates an FXMLLoader for the specified FXML file path.
   *
   * @param fxmlPath the path to the FXML file
   * @return an FXMLLoader for the specified FXML file
   * @throws IllegalArgumentException if the specified resource cannot be loaded
   */
  private def createFXMLLoader(fxmlPath: String): FXMLLoader =
    FXMLLoader(
      Option(this.getClass.getResource(s"/fxml/pages/$fxmlPath.fxml")).getOrElse(
        throw new IllegalArgumentException(
          s"Could not load the specified resource: $fxmlPath. Try with a different path."
        )
      )
    )
