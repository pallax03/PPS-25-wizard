package it.unibo.pps.wizard.application.gui.controllers

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.application.gui.controllers.template.FXMLController
import it.unibo.pps.wizard.engine.model.basic.{Player, PlayerId, PlayerName, Players}
import it.unibo.pps.wizard.engine.model.configuration.GameConfiguration
import scalafx.application.Platform
import javafx.scene.control.{Button, ComboBox, TextField}
import scalafx.stage.Stage

import scala.annotation.nowarn
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class MainPageController(override protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends FXMLController:

  @nowarn @FXML private var nameField: TextField = _
  @nowarn @FXML private var opponentsCombo: ComboBox[Integer] = _
  @nowarn @FXML private var btnStart: Button = _

  @nowarn @FXML
  private def handleStartGameClick(): Unit =
    val playerName = nameField.text.value
    val opponentsNum = opponentsCombo.value.value
    val players = Players(Player.human(PlayerId(1), PlayerName(playerName)))

    println(s"Giocatore: $playerName, Avversari: $opponentsNum")
    context.wizardEngineProxy
      .startGame(players, GameConfiguration(playerName, opponentsNum))
      .onComplete:
        case Success(_) =>
          Platform.runLater {
            // GameBoardView(stage)
            println("Grande nico!!")
          }
        case Failure(exception) => throw exception
