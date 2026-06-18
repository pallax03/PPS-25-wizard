package it.unibo.pps.wizard.engine.services

import io.vertx.core.{AbstractVerticle, Promise}
import it.unibo.pps.wizard.engine.adapters.LocalWizardAdapter
import it.unibo.pps.wizard.engine.model.game.WizardGame
import it.unibo.pps.wizard.engine.ports.WizardPort

class WizardService extends AbstractVerticle:
  private var _wizardAdapter: Option[LocalWizardAdapter] = None

  override def start(startPromise: Promise[Void]): Unit =
    val wizardGame: WizardPort = WizardGame(this.getVertx)
    this._wizardAdapter = Some(LocalWizardAdapter(wizardGame))
    startPromise.complete()

  def localAdapter: Option[LocalWizardAdapter] = this._wizardAdapter
