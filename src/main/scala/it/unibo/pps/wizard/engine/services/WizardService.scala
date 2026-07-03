package it.unibo.pps.wizard.engine.services

import io.vertx.core.{AbstractVerticle, Promise}
import it.unibo.pps.wizard.engine.adapters.WizardGameAdapter

class WizardService extends AbstractVerticle:
  private var _wizardAdapter: Option[WizardGameAdapter] = None

  override def start(startPromise: Promise[Void]): Unit =
    this._wizardAdapter = Some(WizardGameAdapter(this.getVertx))
    startPromise.complete()

  def localAdapter: Option[WizardGameAdapter] = this._wizardAdapter
