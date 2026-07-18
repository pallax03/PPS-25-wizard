package it.unibo.pps.wizard.application.scalafx.util

import javafx.application.Platform
import scalafx.animation.PauseTransition
import scalafx.util.Duration

import scala.util.control.NonFatal

/**
 * The PresentationScript class encapsulates a sequence of presentation steps to be executed.
 *
 * @param steps the list of presentation steps (actions or delays) to execute
 */
final case class PresentationScript(steps: List[PresentationStep])

object PresentationScript:
  def apply(steps: PresentationStep*): PresentationScript = PresentationScript(steps.toList)

/** The PresentationStep trait represents a single step in a presentation, which can be either a runnable action or a time delay. */
sealed trait PresentationStep
object PresentationStep:
  final case class Run(action: () => Unit) extends PresentationStep
  final case class Wait(delayMs: Double) extends PresentationStep

  def run(action: => Unit): PresentationStep = Run(() => action)
  def waitFor(delayMs: Double): PresentationStep = Wait(delayMs)

/** The PresentationQueue class manages and executes a queue of presentation steps sequentially on the JavaFX application thread. */
class PresentationQueue:
  private val queue = scala.collection.mutable.Queue.empty[PresentationStep]
  private var running = false

  def enqueue(script: PresentationScript): Unit =
    onUiThread:
      queue.enqueueAll(script.steps)
      drain()

  private def drain(): Unit =
    if !running && queue.nonEmpty then
      running = true
      run(queue.dequeue())

  private def run(step: PresentationStep): Unit =
    step match
      case PresentationStep.Run(action) =>
        runAction(action)
        running = false
        drain()

      case PresentationStep.Wait(delayMs) =>
        val pause = PauseTransition(toDuration(delayMs))
        pause.onFinished = _ =>
          running = false
          drain()
        pause.play()

  private def runAction(action: () => Unit): Unit =
    try action()
    catch
      case NonFatal(error) =>
        println(s"Presentation step failed: ${error.getMessage}")

  private def toDuration(ms: Double): Duration =
    if ms <= 0 then Duration.Zero else Duration(ms)

  private def onUiThread(action: => Unit): Unit =
    if Platform.isFxApplicationThread then action
    else Platform.runLater(() => action)
