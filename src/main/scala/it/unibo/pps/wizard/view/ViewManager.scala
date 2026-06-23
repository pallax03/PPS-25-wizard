package it.unibo.pps.wizard.view

import it.unibo.pps.wizard.engine.model.basic.Card
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scala.compiletime.uninitialized

object ViewManager extends JFXApp3 {

  private var mainScene: Scene = uninitialized

  override def start(): Unit = {

    // Esempio di una mano di carte reale
    val myHand = List(
      Card(Card.Color.Red, Card.Rank.Ten), // 10 Rosso
      Card.wizard(1), // Un Mago
      Card(Card.Color.Blue, Card.Rank.Thirteen), // 13 Blu
      Card.jester(2), // Un Giullare
      Card(Card.Color.Green, Card.Rank.Two), // 2 Verde
    )
    
    // Funzione di orchestrazione: toglie la Home e mette il Tavolo da Gioco
    def handleStartGame(playerName: String, opponentCount: Int): Unit = {
      // Istanziamo la classe indipendente GameBoard
      mainScene.root = new GameBoardView(playerName, opponentCount, myHand)
    }

    // Inizializzazione della scena con la HomeView
    mainScene = new Scene {
      fill = Color.rgb(28, 28, 28)
      root = new HomeView(handleStartGame)
    }

    stage = new JFXApp3.PrimaryStage {
      title = "PPS Card Game - Wizard"
      width = 1200
      height = 800
      scene = mainScene
    }
  }
}