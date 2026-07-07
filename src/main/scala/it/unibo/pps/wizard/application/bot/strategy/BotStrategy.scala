package it.unibo.pps.wizard.application.bot.strategy

import it.unibo.pps.wizard.engine.events.InvitationEvent
import it.unibo.pps.wizard.engine.model.core.GameAction

trait BotStrategy:
  def decide(invitation: InvitationEvent): GameAction
