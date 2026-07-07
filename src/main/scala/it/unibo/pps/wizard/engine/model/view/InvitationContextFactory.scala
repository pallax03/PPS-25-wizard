package it.unibo.pps.wizard.engine.model.view

import it.unibo.pps.wizard.engine.events.InvitationEvent
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameState
import it.unibo.pps.wizard.engine.model.rules.TableRules.*

object InvitationContextFactory:

  def fromState(state: GameState): Option[InvitationEvent] = state match
    case GameState.Bidding(core, trump: Trump.WizardUnresolved, _, playerId) =>
      Some(
        InvitationEvent.WaitingForTrump(
          TrumpContext(playerId, core.hands.getHand(playerId).head, trump)
        )
      )
    case GameState.Bidding(core, trump, currentBids, playerId) =>
      Some(
        InvitationEvent.WaitingForBid(
          BidContext(
            playerId = playerId,
            hand = core.hands.getHand(playerId).head,
            trump = trump,
            round = core.round,
            currentBids = currentBids,
            totalPlayers = core.players.toList.size,
            dealerId = core.dealerId
          )
        )
      )
    case GameState.Playing(core, trump, bids, table, playerId, tricksWon) =>
      val hand = core.hands.getHand(playerId).head
      Some(
        InvitationEvent.WaitingForCard(
          PlayCardContext(
            playerId = playerId,
            hand = hand,
            legalCards = hand.toList.filter(_.validateAgainst(table, hand).isRight),
            table = table,
            trump = trump,
            bid = bids(playerId),
            tricksWon = tricksWon(playerId),
            currentWinningCard = Option.when(!table.isEmpty)(table.evaluateTrick(trump)._2),
            followingColor = table.followingCard.map(_.color)
          )
        )
      )
    case GameState.Ended(_) =>
      None
