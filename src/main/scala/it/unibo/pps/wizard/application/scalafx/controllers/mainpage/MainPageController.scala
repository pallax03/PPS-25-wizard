package it.unibo.pps.wizard.application.scalafx.controllers.mainpage

import it.unibo.pps.wizard.application.scalafx.WizardApplicationContext
import it.unibo.pps.wizard.application.scalafx.controllers.Controller
import it.unibo.pps.wizard.application.scalafx.pages.GameBoardPage
import it.unibo.pps.wizard.engine.model.basic.{Player, PlayerId, PlayerName, Players}
import it.unibo.pps.wizard.engine.model.configuration.{BotsDifficulty, GameConfiguration}
import javafx.scene.control.{Button, ComboBox, TextField}
import scalafx.stage.Stage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
 * Controller for the main page of the application, responsible for handling user interactions and managing the state of the main page.
 *
 * @param stage the primary stage of the application
 * @param context the application context providing access to various components
 */
class MainPageController(stage: Stage)(using context: WizardApplicationContext)
    extends Controller(stage):

  @nowarn @FXML private var nameField: TextField = _
  @nowarn @FXML private var opponentsCombo: ComboBox[Integer] = _
  @nowarn @FXML private var botsDifficultyCombo: ComboBox[BotsDifficulty] = _
  @nowarn @FXML private var btnStart: Button = _

  /** Initializes the main page controller by setting up the bots difficulty combo box with available options and setting a default value. */
  @FXML
  def initialize(): Unit =
    botsDifficultyCombo.getItems.setAll(BotsDifficulty.values*)
    botsDifficultyCombo.setValue(BotsDifficulty.Dumb)

  /**
   * Handles the click event of the "Start Game" button. It retrieves the player name, number of opponents, and bots difficulty from the UI components,
   * creates a new game configuration, and starts the game by invoking the inbound port's startGame method.
   */
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
    context.botLifecycleManager
      .setupNewBotManager()
      .onComplete:
        case Success(_) =>
          println("Bot manager setup completed successfully.")
          context.inboundPort
            .startGame(players, GameConfiguration(playerName, opponentsNum, botsDifficulty))
            .onComplete:
              case Success(_) =>
                runOnUi:
                  gameBoardPage._1.title =
                    s"${gameBoardPage._1.title.value}: Bots: ${botsDifficultyCombo.value.value}"
                  gameBoardPage._1.show()
              case Failure(exception) =>
                throw exception
        case Failure(exception) =>
          throw exception
