package it.unibo.pps.wizard.engine.model.core

import GameError.*
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.rules.*

object GameEngine:
  def processAction(state: GameState, action: GameAction): Either[GameError, GameState] =
    (state, action) match
      case (currentState: GameState.ChoosingTrump, GameAction.ResolveTrumpColor(playerId, color)) =>
        for
          _ <- currentState.core.dealerId.validateTurnOf(playerId)
          updatedTrump <- currentState.core.trump.resolveWizard(color)
        yield GameState.Bidding(
          currentState.core.updateTrump(updatedTrump),
          Bids.empty,
          currentState.core.dealerId
        )

      case (currentState: GameState.Bidding, GameAction.PlaceBid(playerId, bid)) =>
        for
          _ <- currentState.currentPlayer.validateTurnOf(playerId)
          updatedBids <- BiddingRules.processBid(
            bid,
            currentState.currentBids,
            playerId,
            currentState.core.round,
            currentState.core.players.totalPlayers
          )
        yield
          val totalPlayers = currentState.core.players.totalPlayers
          if updatedBids.isComplete(totalPlayers) then
            val firstPlayer = currentState.core.round.firstPlayer(currentState.core.players)
            GameState.Playing(
              core = currentState.core,
              bids = updatedBids,
              table = Table.empty,
              currentPlayerTurn = firstPlayer,
              tricksWon = Tricks.empty
            )
          else
            val nextPlayer =
              currentState.core.players.nextAfter(playerId).getOrElse(currentState.currentPlayer)
            currentState.copy(
              currentBids = updatedBids,
              currentPlayer = nextPlayer
            )

      case (currentState: GameState.Playing, GameAction.PlayCard(playerId, card)) =>
        val playerHand = currentState.core.hands.getHand(playerId).getOrElse(Hand.empty)
        for
          _ <- currentState.currentPlayerTurn.validateTurnOf(playerId)
          _ <- card.validateAgainst(currentState.table, playerHand)
        yield
          val updatedCore =
            currentState.core.copy(hands = currentState.core.hands.remove(playerId, card))
          val updatedTable = currentState.table + (playerId, card)

          if updatedTable.isTrickComplete(updatedCore.players.totalPlayers) then
            completeTrick(currentState, updatedCore, updatedTable)
          else
            val nextPlayer =
              currentState.core.players
                .nextAfter(playerId)
                .getOrElse(currentState.currentPlayerTurn)
            currentState.copy(
              core = updatedCore,
              table = updatedTable,
              currentPlayerTurn = nextPlayer
            )

      case (_, _) => Left(InvalidAction)

  def initializeGame(players: Players): GameState =
    val round = Round.start
    val core = CoreState(
      players = players,
      hands = Hands.empty,
      deck = Deck.create,
      trump = Trump.Absent,
      round = round,
      dealerId = PlayerId(0),
      scoreboard = Scoreboard.empty
    )
    round.initialize.runA(core).value

  private def completeTrick(
      state: GameState.Playing,
      updatedCore: CoreState,
      completedTable: Table
  ): GameState =
    val winnerId = completedTable.evaluateTrick(updatedCore.trump)._1
    val updatedTricks = state.tricksWon.addTrickTo(winnerId)

    if isRoundComplete(updatedCore.hands) then completeRound(state, updatedCore, updatedTricks)
    else
      state.copy(
        core = updatedCore,
        table = Table.empty,
        currentPlayerTurn = winnerId,
        tricksWon = updatedTricks
      )

  private def completeRound(
      state: GameState.Playing,
      updatedCore: CoreState,
      updatedTricks: Tricks
  ): GameState =
    val updatedScoreboard = ScoringRules.compute(
      updatedCore.players,
      state.bids,
      updatedTricks,
      updatedCore.round,
      updatedCore.scoreboard
    )
    nextRoundOrEnd(updatedCore.copy(scoreboard = updatedScoreboard))

  private def nextRoundOrEnd(core: CoreState): GameState =
    if core.round.isLastRound(core.players) then GameState.Ended(core.players, core.scoreboard)
    else
      val nextRound = core.round.next
      val nextDealer = core.players.nextAfter(core.dealerId).getOrElse(core.dealerId)
      nextRound.initialize.runA(core.copy(round = nextRound, dealerId = nextDealer)).value

  private def isRoundComplete(hands: Hands): Boolean = hands.areEmpty
