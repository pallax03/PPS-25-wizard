package it.unibo.pps.wizard.application.scalafx.util

import javafx.application.Platform
import scalafx.animation.PauseTransition
import scalafx.util.Duration

import scala.util.control.NonFatal

final case class PresentationStep(
    action: () => Unit,
    delayBeforeMs: Double = 0,
    delayAfterMs: Double = 0
)

object PresentationStep:
  def immediate(action: => Unit): PresentationStep = PresentationStep(() => action)
  def before(delayMs: Double)(action: => Unit): PresentationStep =
    PresentationStep(() => action, delayBeforeMs = delayMs)
  def after(delayMs: Double)(action: => Unit): PresentationStep =
    PresentationStep(() => action, delayAfterMs = delayMs)
  val noop: PresentationStep = PresentationStep(() => ())

class PresentationQueue:
  private val queue = scala.collection.mutable.Queue.empty[PresentationStep]
  private var running = false

  def enqueue(step: PresentationStep): Unit =
    onUiThread:
      queue.enqueue(step)
      drain()

  def enqueueAll(steps: Iterable[PresentationStep]): Unit =
    onUiThread:
      queue.enqueueAll(steps)
      drain()

  private def drain(): Unit =
    if !running && queue.nonEmpty then
      running = true
      run(queue.dequeue())

  private def run(step: PresentationStep): Unit =
    val before = PauseTransition(toDuration(step.delayBeforeMs))
    before.onFinished = _ =>
      try step.action()
      catch
        case NonFatal(error) =>
          println(s"Presentation step failed: ${error.getMessage}")
      val after = PauseTransition(toDuration(step.delayAfterMs))
      after.onFinished = _ =>
        running = false
        drain()
      after.play()
    before.play()

  private def toDuration(ms: Double): Duration =
    if ms <= 0 then Duration.Zero else Duration(ms)

  private def onUiThread(action: => Unit): Unit =
    if Platform.isFxApplicationThread then action
    else Platform.runLater(() => action)
