package it.unibo.pps.wizard.application.gui.pages

import cats.data.State
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import it.unibo.pps.wizard.engine.model.basic.*
import scalafx.stage.Stage

object ViewManager:

  def apply(stage: Stage): Unit =

    val drawAction: State[Deck, (List[Card], Option[Card])] = for
      player1Cards <- Deck.pop(6)
      trumpCard <- Deck.pop(1)
    yield (player1Cards, trumpCard.headOption)

    val deck = Deck.create
    val (playerCards1, trumpCard) = drawAction.runA(deck).value

    var mainScene: Scene = null

    def handleStartGame(): Unit =
      if mainScene != null then
        mainScene.root = new GameBoardView(Hand(playerCards1), Table.empty, Trump(trumpCard.head))

    mainScene = new Scene:
      fill = Color.rgb(28, 28, 28)
      root = new HomeView(handleStartGame)

    stage.width = 1200
    stage.height = 800
    stage.scene = mainScene