package it.unibo.pps.wizard.application.scalafx.components

import scalafx.scene.control.Label
import scalafx.scene.paint.Color
import scalafx.scene.text.{Font, FontWeight}

class GameInfoView(var round: Int = 0, var phase: String = "") extends Label:

  private val biddingColor = Color(230 / 255.0, 126 / 255.0, 34 / 255.0, 1)
  private val playingColor = Color(46 / 255.0, 204 / 255.0, 113 / 255.0, 1)

  font = Font.font("Arial", FontWeight.Normal, 25)
  textFill = Color.White
  updateText()

  def incrementRound(newRound: Int): Unit =
    this.round = newRound
    this.updateText()

  def changePhase(newPhase: String): Unit =
    this.phase = newPhase
    textFill = newPhase match
      case "Bidding" => biddingColor
      case "Playing" => playingColor
      case _ => Color.White
    this.updateText()

  private def updateText(): Unit =
    this.text = s"Round: $round\nPhase: $phase"
