package it.unibo.pps.wizard.view

import cats.data.State
import it.unibo.pps.wizard.engine.model.basic.{Card, Deck, Hand, PlayerId, Table, Trump}
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.paint.Color

import scala.compiletime.uninitialized

object ViewManager extends JFXApp3:

  private var mainScene: Scene = uninitialized

  override def start(): Unit =

    val drawAction: State[Deck, (List[Card], Option[Card])] = for
      player1Cards <- Deck.pop(6)
      trumpCard <- Deck.pop(1)
    yield (player1Cards, trumpCard.headOption)

    val deck = Deck()
    val (playerCards1, trumpCard) = drawAction.runA(deck).value

    def handleStartGame(): Unit =
      mainScene.root = new GameBoardView(Hand.fromList(playerCards1), Table.empty, Trump(trumpCard.head))

    mainScene = new Scene:
      fill = Color.rgb(28, 28, 28)
      root = new HomeView(handleStartGame)

    stage = new JFXApp3.PrimaryStage:
      title = "PPS Card Game - Wizard"
      width = 1200
      height = 800
      scene = mainScene