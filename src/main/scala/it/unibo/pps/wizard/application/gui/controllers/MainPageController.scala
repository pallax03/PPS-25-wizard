package it.unibo.pps.wizard.application.gui.controllers

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.application.gui.FXComponent
import it.unibo.pps.wizard.application.gui.pages.GameBoardPage
import it.unibo.pps.wizard.engine.model.basic.{Player, PlayerId, PlayerName, Players}
import it.unibo.pps.wizard.engine.model.configuration.GameConfiguration
import scalafx.application.Platform
import javafx.scene.control.{Button, ComboBox, TextField}
import scalafx.stage.Stage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class MainPageController(protected val stage: Stage)(using
    protected val context: WizardApplicationContext
) extends FXComponent:

  @nowarn @FXML private var nameField: TextField = _
  @nowarn @FXML private var opponentsCombo: ComboBox[Integer] = _
  @nowarn @FXML private var btnStart: Button = _

  @FXML
  def handleStartGameClick(): Unit =
    val playerName = nameField.text.value
    val opponentsNum = opponentsCombo.value.value
    val actualPlayer = Player.human(PlayerId(0), PlayerName(playerName))
    val players = Players(actualPlayer)

    println(s"Configuration:\n  Player Name: $playerName, Opponents: $opponentsNum")
    context.inboundPort
      .startGame(players, GameConfiguration(playerName, opponentsNum))
      .onComplete:
        case Success(_) =>
          Platform.runLater {
            GameBoardPage(stage)
          }
        case Failure(exception) => throw exception
