package it.unibo.pps.wizard.application.gui.components

import scalafx.scene.control.Label
import scalafx.scene.text.{Font, FontWeight}

class GameInfoView(var round: Int = 0, var phase: String = "") extends Label:
  font = Font.font("Arial", FontWeight.Normal, 25)
  textFill = scalafx.scene.paint.Color.White
  updateText()

  def incrementRound(newRound: Int): Unit =
    this.round = newRound
    this.updateText()

  def changePhase(newPhase: String): Unit =
    this.phase = newPhase
    this.updateText()

  private def updateText(): Unit =
    this.text = s"Round: $round\nPhase: $phase"
