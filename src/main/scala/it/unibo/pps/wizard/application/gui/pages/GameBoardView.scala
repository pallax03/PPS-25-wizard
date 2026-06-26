package it.unibo.pps.wizard.application.gui.pages

import it.unibo.pps.wizard.application.gui.components.{HandView, TableView, TrumpView}
import it.unibo.pps.wizard.engine.model.basic.Card.*
import it.unibo.pps.wizard.engine.model.basic.Table.*
import it.unibo.pps.wizard.engine.model.basic.{Card, Hand, PlayerId, Table, Trump}
import scalafx.scene.layout.{BorderPane, StackPane, VBox}
import scalafx.geometry.Insets
import scalafx.geometry.Pos

// todo: need to be passed a controller
class GameBoardView(
                    playerHand: Hand,
                    currentTable: Table,
                    trump: Trump
                  ) extends BorderPane:

  style = "-fx-background-color: #1e1e1e;"
  padding = Insets(20)

  val p1: PlayerId = PlayerId(1)
  val cards: List[Card] = 1.red - 5.green
  private val tableView = new TableView(
    currentTable
      + (p1 plays 1.red)
      + (p1 plays 13.red)
      + (p1 plays wizard)
      + (p1 plays jester)
      + (p1 plays 1.yellow)
      + (p1 plays 1.blue)
  )
  center = new StackPane:
    padding = Insets(20)
    children = tableView


  private val handView = new HandView(playerHand,
    onCardDragged = (mouseX, mouseY) =>
      tableView.setHighlight(tableView.isOver(mouseX, mouseY)),
    onCardDropped = (card, mouseX, mouseY) =>
      tableView.setHighlight(false)
      if tableView.isOver(mouseX, mouseY) then
        println(card) // todo: controller.play
  )
  bottom = new VBox:
    alignment = Pos.Center
    padding = Insets(20, 0, 0, 0)
    children = handView

  private val trumpHolder = new TrumpView(trump)
  left = new VBox:
    alignment = Pos.Center
    padding = Insets(0, 20, 0, 0)
    children = trumpHolder

      //same component but scaled and player can edit (see freeform mockups)
//    val opponentsView = ???
//    val playerView = ???