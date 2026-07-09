package it.unibo.pps.wizard.application.gui.controllers

import it.unibo.pps.wizard.application.WizardApplicationContext
import it.unibo.pps.wizard.application.gui.components.ScoreboardView
import it.unibo.pps.wizard.engine.model.basic.{Players, RoundRow, Scoreboard}
import scalafx.collections.ObservableBuffer
import scalafx.scene.layout.StackPane
import scalafx.stage.Stage

class ScoreboardPageController(stage: Stage)(using context: WizardApplicationContext) extends Controller(stage):
  @nowarn @FXML private var scoreboardContainer: StackPane = _
  @nowarn private var view: ScoreboardView = _

  def init(players: Players): Unit =
    view = new ScoreboardView(players)
    scoreboardContainer.children.add(view)

    refresh(players, Scoreboard.empty)

  def refresh(players: Players, scoreboard: Scoreboard): Unit =
    val rows = RoundRow.createRows(players, scoreboard)
    view.updateData(rows, players.toList.size)
