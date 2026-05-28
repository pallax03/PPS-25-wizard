package unibo.pps.wizard.view

import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.paint.Color

object ViewManager extends JFXApp3 {

  private var mainScene: Scene = _

  override def start(): Unit = {

    // Funzione di orchestrazione: toglie la Home e mette il Tavolo da Gioco
    def handleStartGame(playerName: String, opponentCount: Int): Unit = {
      // Istanziamo la classe indipendente GameBoard
      mainScene.root = new GameBoardView(playerName, opponentCount)
    }

    // Inizializzazione della scena con la HomeView
    mainScene = new Scene {
      fill = Color.rgb(28, 28, 28)
      root = new HomeView(handleStartGame)
    }

    stage = new JFXApp3.PrimaryStage {
      title = "PPS Card Game - Wizard"
      width = 900
      height = 650
      scene = mainScene
    }
  }
}