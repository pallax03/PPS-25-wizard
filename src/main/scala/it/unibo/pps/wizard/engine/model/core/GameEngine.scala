package it.unibo.pps.wizard.engine.model.core

import GameError.*
import it.unibo.pps.wizard.engine.events.*
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.InconsistentStateReasons.{
  HandNotFoundFor,
  TableNoWinner
}
import it.unibo.pps.wizard.engine.model.rules.*

opaque type GameEngine = (GameState, List[WizardEvent])

object GameEngine:

  extension (engine: GameEngine)
    def state: GameState = engine._1
    def events: List[WizardEvent] = engine._2
    private def addEvents(newEvents: WizardEvent*): GameEngine =
      (engine.state, engine.events ++ newEvents)

  def processAction(state: GameState, action: GameAction): Either[GameError, GameEngine] =
    (state, action) match
      case (currentState: GameState.ChoosingTrump, GameAction.ResolveTrumpColor(playerId, color)) =>
        for
          _ <- currentState.core.dealerId.validateTurnOf(playerId)
          updatedTrump <- currentState.core.trump resolveWizard color
        yield (
          GameState.Bidding(
            currentState.core.updateTrump(updatedTrump),
            Bids.empty,
            currentState.core.dealerId
          ),
          List(
            ActionEvent.TrumpColorResolved(playerId, color)
          )
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
            val hand = currentState.core.hands.getHand(firstPlayer).head
            (
              GameState.Playing(
                core = currentState.core,
                bids = updatedBids,
                table = Table.empty,
                currentPlayerTurn = firstPlayer,
                tricksWon = Tricks.empty
              ),
              List(
                ProgressEvent.PhaseChanged(GameState.Playing.toString),
                ActionEvent.BidPlaced(playerId, bid),
                ProgressEvent.IsTurnOf(firstPlayer),
                InvitationEvent.WaitingForCard(
                  firstPlayer,
                  hand.toList.filter(_.validateAgainst(Table.empty, hand).isRight)
                )
              )
            )
          else
            val nextPlayer =
              currentState.core.players.nextAfter(playerId).getOrElse(currentState.currentPlayer)
            (
              currentState.copy(
                currentBids = updatedBids,
                currentPlayer = nextPlayer
              ),
              List(
                ActionEvent.BidPlaced(playerId, bid),
                ProgressEvent.IsTurnOf(nextPlayer),
                InvitationEvent.WaitingForBid(nextPlayer)
              )
            )

      case (currentState: GameState.Playing, GameAction.PlayCard(playerId, card)) =>
        val playerHand = currentState.core.hands.getHand(playerId).getOrElse(Hand.empty)
        for
          _ <- currentState.currentPlayerTurn.validateTurnOf(playerId)
          _ <- card.validateAgainst(currentState.table, playerHand)

          updatedCore = currentState.core.copy(hands =
            currentState.core.hands.remove(playerId, card)
          )
          updatedTable = currentState.table + (playerId, card)

          finalEngine <-
            if updatedTable.isTrickComplete(updatedCore.players.totalPlayers) then
              completeTrick(currentState, updatedCore, updatedTable).map: engine =>
                val nextPlayer = currentState.core.players
                  .nextAfter(playerId)
                  .getOrElse(currentState.currentPlayerTurn)
                engine.addEvents(
                  ProgressEvent.IsTurnOf(nextPlayer),
                  ActionEvent.CardPlayed(playerId, card)
                )
            else
              val nextPlayer = currentState.core.players
                .nextAfter(playerId)
                .getOrElse(currentState.currentPlayerTurn)
              updatedCore.hands
                .getHand(nextPlayer)
                .toRight(GameError.InconsistentState(HandNotFoundFor(nextPlayer)))
                .map: hand =>
                  (
                    currentState.copy(
                      core = updatedCore,
                      table = updatedTable,
                      currentPlayerTurn = nextPlayer
                    ),
                    List(
                      ActionEvent.CardPlayed(playerId, card),
                      ProgressEvent.IsTurnOf(nextPlayer),
                      InvitationEvent.WaitingForCard(
                        nextPlayer,
                        hand.toList.filter(_.validateAgainst(Table.empty, hand).isRight)
                      )
                    )
                  )
        yield finalEngine

      case (_, _) => Left(InvalidAction)

  def initializeGame(players: Players): GameEngine =
    val round = Round.start

    val (core, gameState) =
      round.initialize(Deck.create).run(CoreState.initialize(players, round)).value

    val specificEvents = gameState match
      case _: GameState.ChoosingTrump => List(InvitationEvent.WaitingForTrump(core.dealerId))
      case _: GameState.Bidding       => List(InvitationEvent.WaitingForBid(core.dealerId))
      case _                          => Nil

    (gameState, specificEvents).addEvents(
      ProgressEvent.CardsDealt(core.dealerId, core.hands, core.trump, core.round),
      ProgressEvent.PhaseChanged(gameState.getClass.getSimpleName)
    )

  private def completeTrick(
      state: GameState.Playing,
      updatedCore: CoreState,
      completedTable: Table
  ): Either[GameError, GameEngine] =
    for
      winningCard <- completedTable
        .evaluateTrick(updatedCore.trump)
        .toRight(GameError.InconsistentState(TableNoWinner))

      winnerId <- completedTable
        .playerOf(winningCard)
        .toRight(GameError.InconsistentState(TableNoWinner))

      engine <-
        if isRoundComplete(updatedCore.hands) then
          val updatedTricks = state.tricksWon.addTrickTo(winnerId)
          Right(
            completeRound(state, updatedCore, updatedTricks).addEvents(
              ProgressEvent.TrickWon(winnerId, completedTable.playedCards)
            )
          )
        else
          updatedCore.hands
            .getHand(winnerId)
            .toRight(GameError.InconsistentState(HandNotFoundFor(winnerId)))
            .map: hand =>
              val updatedTricks = state.tricksWon.addTrickTo(winnerId)
              (
                state.copy(
                  core = updatedCore,
                  table = Table.empty,
                  currentPlayerTurn = winnerId,
                  tricksWon = updatedTricks
                ),
                List(
                  ProgressEvent.TrickWon(winnerId, completedTable.playedCards),
                  ProgressEvent.IsTurnOf(winnerId),
                  InvitationEvent.WaitingForCard(
                    winnerId,
                    hand.toList.filter(_.validateAgainst(Table.empty, hand).isRight)
                  )
                )
              )
    yield engine

  private def completeRound(
      state: GameState.Playing,
      updatedCore: CoreState,
      updatedTricks: Tricks
  ): GameEngine =
    val updatedScoreboard = ScoringRules.compute(
      updatedCore.players,
      state.bids,
      updatedTricks,
      updatedCore.round,
      updatedCore.scoreboard
    )
    nextRoundOrEnd(updatedCore.copy(scoreboard = updatedScoreboard)).addEvents(
      ProgressEvent.RoundScored(updatedScoreboard)
    )

  private def nextRoundOrEnd(core: CoreState): GameEngine =
    if core.round.isLastRound(core.players) then
      (GameState.Ended(core.players, core.scoreboard), List())
    else
      val nextRound = core.round.next
      val nextDealer = core.players.nextAfter(core.dealerId).getOrElse(core.dealerId)
      val (newCore, gameState) = nextRound
        .initialize(Deck.create)
        .run(core.copy(round = nextRound, dealerId = nextDealer))
        .value

      val specificEvents = gameState match
        case _: GameState.ChoosingTrump => List(InvitationEvent.WaitingForTrump(nextDealer))
        case _: GameState.Bidding       => List(InvitationEvent.WaitingForBid(nextDealer))
        case _                          => Nil

      (gameState, specificEvents).addEvents(
        ProgressEvent.CardsDealt(newCore.dealerId, newCore.hands, newCore.trump, newCore.round),
        ProgressEvent.PhaseChanged(gameState.getClass.getSimpleName)
      )

  private def isRoundComplete(hands: Hands): Boolean = hands.areEmpty
