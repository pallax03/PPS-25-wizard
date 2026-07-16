package it.unibo.pps.wizard.application.scalafx.components

import scalafx.animation.FadeTransition
import scalafx.animation.PauseTransition
import scalafx.animation.SequentialTransition
import scalafx.scene.control.Label
import scalafx.util.Duration

import scala.annotation.nowarn

class MessageLabel extends Label:
  private val baseStyle = "-fx-background-color: rgba(0, 0, 0, 0); " +
    "-fx-font-size: 20px; " +
    "-fx-font-weight: bold; " +
    "-fx-padding: 5px 20px;"

  style = baseStyle
  opacity = 0.0

  @nowarn private var activeTransition: SequentialTransition = _

  def show(message: String, textColor: String, onFinishedAction: => Unit): Unit =
    if activeTransition != null then activeTransition.stop()

    text = message
    style = baseStyle + s" -fx-text-fill: $textColor;"
    opacity = 0.0

    val fadeIn = new FadeTransition(Duration(200), this):
      toValue = 1.0
    val hold = new PauseTransition(Duration(2500))
    val fadeOut = new FadeTransition(Duration(500), this):
      toValue = 0.0

    fadeOut.onFinished = _ => onFinishedAction

    activeTransition = new SequentialTransition:
      children = Seq(fadeIn, hold, fadeOut)

    activeTransition.play()

  def cancel(): Unit =
    if activeTransition != null then
      activeTransition.stop()
      activeTransition = null
    opacity = 0.0
