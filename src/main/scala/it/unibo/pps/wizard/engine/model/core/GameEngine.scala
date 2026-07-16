package it.unibo.pps.wizard.engine.model.core

import GameError.*
import it.unibo.pps.wizard.engine.events.*
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.basic.cards.*
import it.unibo.pps.wizard.engine.model.basic.gameplay.{Round, Table}
import it.unibo.pps.wizard.engine.model.core.InconsistentStateReasons.{
  HandNotFoundFor,
  TableNoWinner
}
import it.unibo.pps.wizard.engine.model.rules.*

/**
 * The GameEngine is responsible for processing game actions and managing the game state.
 * It takes a GameState and a GameAction as input and produces either a new GameEngine or a GameError.
 */
opaque type GameEngine = (GameState, List[WizardEvent])

/**
 * Companion object for the GameEngine opaque type.
 * Provides methods to process game actions and manage the game state.
 */
object GameEngine:

  extension (engine: GameEngine)
    def state: GameState = engine._1
    def events: List[WizardEvent] = engine._2

  /**
   * Processes a game action based on the current game state.
   * Returns either a new GameEngine or a GameError if the action is invalid.
   *
   * @param state The current game state.
   * @param action The game action to be processed.
   * @return Either a new GameEngine or a GameError.
   */
  def processAction(state: GameState, action: GameAction): Either[GameError, GameEngine] =
    (state, action) match
      case (currentState: GameState.ChoosingTrump, GameAction.ResolveTrumpColor(playerId, color)) =>
        handleResolveTrump(currentState, playerId, color)

      case (currentState: GameState.Bidding, GameAction.PlaceBid(playerId, bid)) =>
        handlePlaceBid(currentState, playerId, bid)

      case (currentState: GameState.Playing, GameAction.PlayCard(playerId, card)) =>
        handlePlayCard(currentState, playerId, card)

      case (_, _) => Left(InvalidAction)

  /**
   * Handles the action of playing a card during the Playing phase.
   * Validates the player's turn and the card being played, updates the game state accordingly,
   * and generates relevant events.
   *
   * @param currentState The current game state in the Playing phase.
   * @param playerId The ID of the player attempting to play a card.
   * @param card The card being played by the player.
   * @return Either a new GameEngine or a GameError if the action is invalid.
   */
  private def handlePlayCard(
      currentState: GameState.Playing,
      playerId: PlayerId,
      card: Card
  ): Either[GameError, GameEngine] =
    val playerHand = currentState.core.hands.getHand(playerId).getOrElse(Hand.empty)
    for
      _ <- currentState.currentPlayerTurn.validateTurnOf(playerId)
      _ <- card.validateAgainst(currentState.table, playerHand)
      updatedHands <- currentState.core.hands
        .remove(playerId, card)
        .toRight(GameError.InconsistentState(HandNotFoundFor(playerId)))

      updatedCore = currentState.core.copy(hands = updatedHands)
      updatedTable = currentState.table + (playerId, card)
      winningCard = updatedTable.evaluateTrick(currentState.core.trump)
      followingColor = updatedTable.followingColor
      playerName = currentState.core.players
        .findById(playerId)
        .map(_.name)
        .getOrElse(PlayerName("Unknown"))
      finalEngine <-
        if updatedTable.isTrickComplete(updatedCore.players.totalPlayers) then
          completeTrick(currentState, updatedCore, updatedTable).map: engine =>
            (
              engine.state,
              ActionEvent.CardPlayed(
                playerId,
                playerName,
                card,
                winningCard,
                followingColor
              ) +: engine.events
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
                  ActionEvent
                    .CardPlayed(playerId, playerName, card, winningCard, followingColor),
                  InvitationEvent.WaitingForCard(
                    nextPlayer,
                    hand.legalCards(updatedTable)
                  )
                )
              )
    yield finalEngine

  /**
   * Handles the action of placing a bid during the Bidding phase.
   * Validates the player's turn and the bid being placed, updates the game state accordingly,
   * and generates relevant events.
   *
   * @param currentState The current game state in the Bidding phase.
   * @param playerId The ID of the player attempting to place a bid.
   * @param bid The bid being placed by the player.
   * @return Either a new GameEngine or a GameError if the action is invalid.
   */
  private def handlePlaceBid(
      currentState: GameState.Bidding,
      playerId: PlayerId,
      bid: Bid
  ): Either[GameError, GameEngine] =
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
            InvitationEvent.WaitingForCard(
              firstPlayer,
              hand.legalCards(Table.empty)
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
            InvitationEvent.WaitingForBid(nextPlayer, currentState.core.round)
          )
        )

  /**
   * Handles the action of resolving the trump color during the ChoosingTrump phase.
   * Validates the player's turn and the color being resolved, updates the game state accordingly,
   * and generates relevant events.
   *
   * @param currentState The current game state in the ChoosingTrump phase.
   * @param playerId The ID of the player attempting to resolve the trump color.
   * @param color The color being resolved as the trump.
   * @return Either a new GameEngine or a GameError if the action is invalid.
   */
  private def handleResolveTrump(
      currentState: GameState.ChoosingTrump,
      playerId: PlayerId,
      color: Card.Color
  ): Either[GameError, GameEngine] =
    for
      _ <- currentState.core.dealerId.validateTurnOf(playerId)
      updatedTrump <- currentState.core.trump resolveWizard color
    yield
      val nextState = GameState.Bidding(
        currentState.core.updateTrump(updatedTrump),
        Bids.empty,
        currentState.core.dealerId
      )
      (
        nextState,
        List(
          ActionEvent.TrumpColorResolved(playerId, color),
          ProgressEvent.PhaseChanged(nextState.getClass.getSimpleName),
          InvitationEvent.WaitingForBid(nextState.currentPlayer, nextState.core.round)
        )
      )

  /**
   * Initializes the game engine with the given players.
   * Deals cards, sets the initial game state, and generates relevant events.
   *
   * @param players The players participating in the game.
   * @return A new GameEngine with the initial game state and events.
   */
  def initializeGame(players: Players): GameEngine =
    val round = Round.start

    val (core, gameState) =
      round.initialize(Deck.create).run(CoreState.initialize(players, round)).value

    val specificEvents = gameState match
      case _: GameState.ChoosingTrump =>
        List(
          InvitationEvent.WaitingForTrump(core.dealerId)
        )
      case bidding: GameState.Bidding =>
        List(
          InvitationEvent.WaitingForBid(bidding.currentPlayer, round)
        )
      case _ => Nil

    (
      gameState,
      List(
        ProgressEvent.CardsDealt(core.dealerId, core.hands, core.trump, core.round),
        ProgressEvent.PhaseChanged(gameState.getClass.getSimpleName)
      ) ++ specificEvents
    )

  /**
   * Completes a trick in the Playing phase.
   * Evaluates the trick, determines the winner, and updates the game state accordingly.
   *
   * @param state The current game state in the Playing phase.
   * @param updatedCore The updated core state.
   * @param completedTable The table with the completed trick.
   * @return Either a new GameEngine or a GameError if the action is invalid.
   */
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

      updatedTricks = state.tricksWon.addTrickTo(winnerId)

      engine <-
        if isRoundComplete(updatedCore.hands) then
          val completedRound = completeRound(state, updatedCore, updatedTricks)
          Right(
            (
              completedRound.state,
              ProgressEvent.TrickWon(
                winnerId,
                updatedTricks(winnerId),
                completedTable.playedCards
              ) +: completedRound.events
            )
          )
        else
          updatedCore.hands
            .getHand(winnerId)
            .toRight(GameError.InconsistentState(HandNotFoundFor(winnerId)))
            .map: hand =>
              (
                state.copy(
                  core = updatedCore,
                  table = Table.empty,
                  currentPlayerTurn = winnerId,
                  tricksWon = updatedTricks
                ),
                List(
                  ProgressEvent
                    .TrickWon(winnerId, updatedTricks(winnerId), completedTable.playedCards),
                  InvitationEvent.WaitingForCard(
                    winnerId,
                    hand.toList.filter(_.validateAgainst(Table.empty, hand).isRight)
                  )
                )
              )
    yield engine

  /**
   * Completes a round in the Playing phase.
   * Computes the updated scoreboard, determines the next round or end of the game, and updates the game state accordingly.
   *
   * @param state The current game state in the Playing phase.
   * @param updatedCore The updated core state.
   * @param updatedTricks The updated tricks.
   * @return A new GameEngine with the completed round and relevant events.
   */
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
    val next = nextRoundOrEnd(updatedCore.copy(scoreboard = updatedScoreboard))
    (next.state, ProgressEvent.RoundScored(updatedScoreboard, updatedCore.players) +: next.events)

  private def nextRoundOrEnd(core: CoreState): GameEngine =
    if core.round.isLastRound(core.players) then
      (
        GameState.Ended(core.players, core.scoreboard),
        List(LifecycleEvent.GameEnded(core.scoreboard, core.players))
      )
    else
      val nextRound = core.round.next
      val nextDealer = core.players.nextAfter(core.dealerId).getOrElse(core.dealerId)
      val (newCore, gameState) = nextRound
        .initialize(Deck.create)
        .run(core.copy(round = nextRound, dealerId = nextDealer))
        .value

      val specificEvents = gameState match
        case _: GameState.ChoosingTrump =>
          List(
            InvitationEvent.WaitingForTrump(nextDealer)
          )
        case bidding: GameState.Bidding =>
          List(
            InvitationEvent.WaitingForBid(bidding.currentPlayer, nextRound)
          )
        case _ => Nil

      (
        gameState,
        List(
          ProgressEvent.CardsDealt(newCore.dealerId, newCore.hands, newCore.trump, newCore.round),
          ProgressEvent.PhaseChanged(gameState.getClass.getSimpleName)
        ) ++ specificEvents
      )

  /**
   * Checks if the round is complete by verifying if all players' hands are empty.
   *
   * @param hands The hands of all players.
   * @return True if the round is complete, false otherwise.
   */
  private def isRoundComplete(hands: Hands): Boolean = hands.areEmpty
