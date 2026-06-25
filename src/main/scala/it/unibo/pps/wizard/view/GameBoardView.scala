package it.unibo.pps.wizard.view

import scalafx.scene.layout.{BorderPane, StackPane, VBox}
import scalafx.geometry.Insets
import scalafx.geometry.Pos
import it.unibo.pps.wizard.engine.model.basic.{Hand, Table, Trump}
import it.unibo.pps.wizard.view.components.{HandView, TableView, TrumpView}

class GameBoardView(
                    playerHand: Hand,
                    currentTable: Table,
                    trump: Trump
                  ) extends BorderPane:

  style = "-fx-background-color: #1e1e1e;"
  padding = Insets(20)

  val tableView = new TableView(currentTable)
  center = new StackPane:
    padding = Insets(20)
    children = tableView

  val handView = new HandView(playerHand,
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

  val trumpHolder = new TrumpView(trump)
  left = new VBox:
    alignment = Pos.Center
    padding = Insets(0, 20, 0, 0)
    children = trumpHolder

      //same component but scaled and player can edit (see freeform mockups)
//    val opponentsView = ???
//    val playerView = ???