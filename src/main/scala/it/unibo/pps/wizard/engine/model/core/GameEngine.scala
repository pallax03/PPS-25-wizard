package it.unibo.pps.wizard.engine.model.core

import GameError.*
import it.unibo.pps.wizard.engine.model.basic.{Deck, Hand, Hands, PlayerId, Players, Round, Scoreboard, Table, Tricks}
import it.unibo.pps.wizard.engine.model.rules.{BiddingRules, ScoringRules}
import it.unibo.pps.wizard.engine.model.rules.RoundManager.*
import it.unibo.pps.wizard.engine.model.rules.TableRules.{evaluateTrickWinner, validateAgainst}

object GameEngine:
  def processAction(state: GameState, action: GameAction): Either[GameError, GameState] =
    (state, action) match
      case (currentState: GameState.Bidding, GameAction.PlaceBid(playerId, bid)) =>
        for
          _ <- currentState.currentPlayer.validateTurnOf(playerId)
          updatedBids <- BiddingRules.processBid(
            bid,
            currentState.currentBids,
            playerId,
            currentState.core.round,
            currentState.core.players.toList.size
          )
        yield
          val totalPlayers = currentState.core.players.toList.size
          if updatedBids.isComplete(totalPlayers) then
            val firstPlayer = currentState.core.round.firstPlayer(currentState.core.players)
            GameState.Playing(
              core = currentState.core,
              trump = currentState.trump,
              bids = updatedBids,
              table = Table.empty,
              currentPlayerTurn = firstPlayer,
              tricksWon = Tricks.empty
            )
          else
            val nextPlayer = currentState.core.players.nextAfter(playerId).getOrElse(currentState.currentPlayer)
            currentState.copy(
              currentBids = updatedBids,
              currentPlayer = nextPlayer
            )

      case (currentState: GameState.Bidding, GameAction.ChooseTrump(playerId, color)) =>
        for
          _ <- currentState.currentPlayer.validateTurnOf(playerId)
          updatedTrump <- currentState.trump.resolveWizard(color)
        yield
          currentState.copy(trump = updatedTrump)

      case (currentState: GameState.Playing, GameAction.PlayCard(playerId, card)) =>
        val playerHand = currentState.core.hands.getHand(playerId).getOrElse(Hand.empty)
        val totalPlayers = currentState.core.players.toList.size
        for
          _ <- currentState.currentPlayerTurn.validateTurnOf(playerId)
          _ <- card.validateAgainst(currentState.table, playerHand)
        yield
          val updatedHands = currentState.core.hands.remove(playerId, card)
          val updatedTable = currentState.table + (playerId, card)
          val updatedCore = currentState.core.copy(hands = updatedHands)

          if updatedTable.size == totalPlayers then
            val winnerId = updatedTable.evaluateTrickWinner(currentState.trump)
            val updatedTricks = currentState.tricksWon.addTrickTo(winnerId)

            if updatedHands.areEmpty then
              val updatedScoreboard = ScoringRules.compute(
                updatedCore.players,
                currentState.bids,
                updatedTricks,
                updatedCore.scoreboard
              )

              val finalCore = updatedCore.copy(scoreboard = updatedScoreboard)

              if finalCore.round.value == (Deck.create.length / totalPlayers) then
                GameState.Ended(finalCore.scoreboard)
              else
                val nextRound = finalCore.round.next
                val coreForNextRound = finalCore.copy(
                  round = nextRound,
                  dealerId = finalCore.players.nextAfter(finalCore.dealerId).getOrElse(PlayerId(0)),
                )
                val (_, nextBiddingState) = nextRound.initialize.run(coreForNextRound).value
                nextBiddingState
            else
              GameState.Playing(
                core = updatedCore,
                trump = currentState.trump,
                bids = currentState.bids,
                table = Table.empty,
                currentPlayerTurn = winnerId,
                tricksWon = updatedTricks
              )
          else
            val nextPlayer = updatedCore.players.nextAfter(playerId).getOrElse(currentState.currentPlayerTurn)
            currentState.copy(
              core = updatedCore,
              table = updatedTable,
              currentPlayerTurn = nextPlayer
            )

      case (GameState.Ended(_), _) =>
        Left(InvalidAction)

      case (_, _)               =>
        Left(InvalidAction)

  def initializeGame(players: Players): GameState =
    val round = Round.start
    val core = CoreState(
      players = players,
      hands = Hands.empty,
      deck = Deck.create,
      round = round,
      dealerId = PlayerId(0),
      scoreboard = Scoreboard.empty
    )
    val (_, biddingState) = round.initialize.run(core).value
    biddingState
