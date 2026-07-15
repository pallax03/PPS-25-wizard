package it.unibo.pps.wizard.application.scalafx.components

import it.unibo.pps.wizard.application.scalafx.util.{UiPhase, WizardTheme}
import scalafx.scene.control.Label
import scalafx.scene.text.{Font, FontWeight}

class GameInfoView(var round: Int = 0, var phase: UiPhase = UiPhase.Unknown) extends Label:

  font = Font.font("Arial", FontWeight.Normal, 25)
  textFill = WizardTheme.Colors.white
  updateText()

  def incrementRound(newRound: Int): Unit =
    this.round = newRound
    this.updateText()

  def changePhase(newPhase: UiPhase): Unit =
    this.phase = newPhase
    textFill = WizardTheme.GameInfo.colorFor(newPhase)
    this.updateText()

  private def updateText(): Unit =
    this.text = s"Round: $round\nPhase: ${phase.label}"
