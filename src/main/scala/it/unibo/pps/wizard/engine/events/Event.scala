package it.unibo.pps.wizard.engine.events

import scala.reflect.{ClassTag, classTag}

trait Event
  
object Event:
  def addressOf[T <: Event: ClassTag]: String = classTag[T].runtimeClass.getSimpleName