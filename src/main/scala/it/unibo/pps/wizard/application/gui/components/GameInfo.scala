package it.unibo.pps.wizard.application.gui.components

import scalafx.scene.control.Label
import scalafx.scene.text.{Font, FontWeight}

object GameInfo:
  def apply(round: Int, phase: String): Label =
    new Label(s"Round: $round\nPhase: $phase"):
      font = Font.font("Arial", FontWeight.Normal, 25)
      textFill = scalafx.scene.paint.Color.White

  def incrementRound(label: Label, newRound: Int): Unit =
    val currentText = label.getText
    val roundPattern = """Round: (\d+)""".r
    val newText = roundPattern.replaceFirstIn(currentText, s"Round: $newRound")
    label.setText(newText)

  def changePhase(label: Label, newPhase: String): Unit =
    val currentText = label.getText
    val phasePattern = """Phase: (\w+)""".r
    val newText = phasePattern.replaceFirstIn(currentText, s"Phase: $newPhase")
    label.setText(newText)
