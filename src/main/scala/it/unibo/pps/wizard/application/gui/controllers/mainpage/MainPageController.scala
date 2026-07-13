package it.unibo.pps.wizard.application.gui.controllers.mainpage

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.application.gui.controllers.Controller
import it.unibo.pps.wizard.application.gui.pages.GameBoardPage
import it.unibo.pps.wizard.engine.model.basic.{Player, PlayerId, PlayerName, Players}
import it.unibo.pps.wizard.engine.model.configuration.{BotsDifficulty, GameConfiguration}
import javafx.scene.control.{Button, ComboBox, TextField}
import scalafx.stage.Stage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class MainPageController(stage: Stage)(using context: WizardApplicationContext)
    extends Controller(stage):

  @nowarn @FXML private var nameField: TextField = _
  @nowarn @FXML private var opponentsCombo: ComboBox[Integer] = _
  @nowarn @FXML private var botsDifficultyCombo: ComboBox[BotsDifficulty] = _
  @nowarn @FXML private var btnStart: Button = _

  @FXML
  def initialize(): Unit =
    botsDifficultyCombo.getItems.setAll(BotsDifficulty.values*)
    botsDifficultyCombo.setValue(BotsDifficulty.Dumb)

  @FXML
  def handleStartGameClick(): Unit =
    val playerName = nameField.text.value
    val opponentsNum = opponentsCombo.value.value
    val botsDifficulty = botsDifficultyCombo.value.value
    val currentPlayerId = PlayerId(0)
    val actualPlayer = Player.human(currentPlayerId, PlayerName(playerName))
    val players = Players(actualPlayer)
    val gameBoardPage = GameBoardPage(stage, currentPlayerId)
    
    println(s"Configuration:\n  Player Name: $playerName, Opponents: $opponentsNum")
    context.inboundPort
      .startGame(players, GameConfiguration(playerName, opponentsNum, botsDifficulty))
      .onComplete:
        case Success(_) =>
          runOnUi:
            gameBoardPage._1.show()
        case Failure(exception) =>
          throw exception
