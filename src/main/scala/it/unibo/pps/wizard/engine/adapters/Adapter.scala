package it.unibo.pps.wizard.engine.adapters

trait Adapter[Port]:
  
  protected def port: Port