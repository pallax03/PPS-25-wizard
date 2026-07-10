package it.unibo.pps.wizard.engine.model.view

import it.unibo.pps.wizard.engine.events.InvitationEvent
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameState
import it.unibo.pps.wizard.engine.model.rules.TableRules.*

object InvitationContextFactory:

  def fromState(state: GameState): Option[InvitationEvent] = state match
    case GameState.ChoosingTrump(core) =>
      Some(
        InvitationEvent.WaitingForTrump(
          core.dealerId,
          TrumpContext(core.dealerId, core.hands.getHand(core.dealerId).head, core.trump)
        )
      )
    case GameState.Bidding(core, currentBids, playerId) =>
      Some(
        InvitationEvent.WaitingForBid(
          core.dealerId,
          BidContext(
            playerId = playerId,
            hand = core.hands.getHand(playerId).head,
            trump = core.trump,
            round = core.round,
            currentBids = currentBids,
            totalPlayers = core.players.toList.size,
            dealerId = core.dealerId
          )
        )
      )
    case GameState.Playing(core, bids, table, playerId, tricksWon) =>
      val hand = core.hands.getHand(playerId).head
      Some(
        InvitationEvent.WaitingForCard(
          playerId, 
          PlayCardContext(
            playerId = playerId,
            hand = hand,
            legalCards = hand.toList.filter(_.validateAgainst(table, hand).isRight),
            table = table,
            trump = core.trump,
            bid = bids(playerId),
            tricksWon = tricksWon(playerId),
            currentWinningCard = Option.when(!table.isEmpty)(table.evaluateTrick(core.trump)._2),
            followingColor = table.followingCard.map(_.color)
          )
        )
      )
    case GameState.Ended(_, _) => None
