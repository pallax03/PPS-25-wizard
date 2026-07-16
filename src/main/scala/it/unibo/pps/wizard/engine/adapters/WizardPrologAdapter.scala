package it.unibo.pps.wizard.engine.adapters

import it.unibo.pps.wizard.engine.model.basic.gameplay.{Round, Table}
import it.unibo.pps.wizard.engine.model.basic.cards.{Card, Hand}
import it.unibo.pps.wizard.engine.model.basic.{Bid, Bids, PlayerId}
import it.unibo.pps.wizard.engine.model.core.GameState
import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardInboundPort}
import it.unibo.pps.wizard.engine.prolog.WizardPrologEngine
import it.unibo.pps.wizard.engine.model.rules.BiddingRules.*
import it.unibo.pps.wizard.engine.model.rules.TableRules.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Adapter that connects the game engine's AI requirements with the Prolog knowledge base.
 *
 * This Adapter work with [[WizardGameAdapter]] as every api need to get actual state to respond with the correct data for the correct playerId request.
 * @throws Future[Exception] Each api return a failed future if any problem occurs.
 *
 * This component acts as a safety layer:
 * 1. Validates that the AI requests are performed during the correct game phases.
 * 2. Manages interactions with the [[WizardPrologEngine]].
 * 3. Provides robust fallbacks: if Prolog fails to return a valid move, this adapter ensures the game continues by providing a valid default move.
 */
class WizardPrologAdapter(private val inboundPort: WizardInboundPort) extends WizardAIPort:

  private val engine = WizardPrologEngine()

  /**
   * Ensures that AI actions are only executed when the game is in the 'Running' phase.
   *
   * @throws IllegalStateException
   *  - if [[WizardGameAdapter]] is not in a valid Running(state)
   *  - if any api is not be called in a specific [[GameState]]
   */
  private def onRunningPhase[T](actionName: String)(
      phaseLogic: PartialFunction[GameState, Future[T]]
  ): Future[T] =
    inboundPort.getState.flatMap:
      case WizardGameState.Running(state) =>
        phaseLogic.applyOrElse(
          state,
          _ => Future.failed(IllegalStateException(s"Cannot $actionName: invalid game phase"))
        )
      case _ => Future.failed(IllegalStateException("Game is not running"))

  /** Helper to retrieve the player's hand, failing the future if the player is missing. */
  private def withHand[T](handOpt: Option[Hand])(prologLogic: Hand => T): Future[T] =
    handOpt match
      case Some(hand) => Future.successful(prologLogic(hand))
      case None       => Future.failed(IllegalArgumentException("Player not found in game state"))

  /**
   * @param playerId given a player, retrieve every playerId's data
   * @return the best Color to resolve trump
   * @note falls back to the first Card.Color.values
   */
  override def resolvedTrumpColor(playerId: PlayerId): Future[Card.Color] =
    onRunningPhase("choose trump color"):
      case GameState.ChoosingTrump(core) =>
        withHand(core.hands.getHand(playerId)): hand =>
          engine.chooseTrumpColor(hand).getOrElse(Card.Color.values.head)

  /**
   * Queries Prolog to suggest a bid for the specified player.
   *
   * @param playerId the ID of the player requesting the bid.
   * @return A suggested [[Bid]].
   * @see [[adjustBid]] for adjest teh suggested bid to a valid bid, based on the suggested.
   * @note falls back to [[firstValidBid]].
   */
  override def placeBid(playerId: PlayerId): Future[Bid] =
    onRunningPhase("place bid"):
      case GameState.Bidding(core, currentBids, _) =>
        withHand(core.hands.getHand(playerId)): hand =>
          engine
            .placeBid(hand, core.trump)
            .getOrElse(firstValidBid(core.round, currentBids, core.players.totalPlayers))

  /**
   * Queries Prolog to provide an adjusted bid after the initial bid was rejected.
   *
   * @param playerId the ID of the player requesting the adjustment.
   * @return A valid [[Bid]] that satisfies game constraints.
   * @note falls back to [[firstValidBid]].
   */
  override def adjustBid(playerId: PlayerId): Future[Bid] =
    onRunningPhase("adjust bid"):
      case GameState.Bidding(core, currentBids, _) =>
        withHand(core.hands.getHand(playerId)): hand =>
          val rejectedBid = Bid(core.round.value - currentBids.total)
          engine
            .adjustBid(hand, rejectedBid)
            .filter(_.validateBid(core.round, currentBids, core.players.totalPlayers).isRight)
            .getOrElse(firstValidBid(core.round, currentBids, core.players.totalPlayers))

  /**
   * Evaluates the best legal card to play from the player's hand.
   *
   * @param playerId the ID of the player.
   * @return The best [[Card]] to play.
   * @note If Prolog fails or suggests a card not in `legalCards`, it falls back to
   *       playing the first legal card available.
   */
  override def bestCard(playerId: PlayerId): Future[Card] =
    onRunningPhase("play best card"):
      case GameState.Playing(core, bids, table, _, tricks) =>
        withHand(core.hands.getHand(playerId)): hand =>
          val legalCards = hand.legalCards(table)
          engine
            .bestPlayableCard(
              hand = hand,
              winningCard = table.evaluateTrick(core.trump),
              followingColor = table.followingColor,
              trump = core.trump,
              playerBid = bids(playerId),
              playerTrick = tricks(playerId)
            )
            .filter(legalCards.contains)
            .getOrElse(legalCards.head)

  /**
   * Calculate a valid Bid for the specified round.
   * @param round current round
   * @param bids every bid
   * @param totalPlayers number of the total players
   * @return a valid bid.
   */
  private def firstValidBid(round: Round, bids: Bids, totalPlayers: Int): Bid =
    (0 to round.value)
      .map(Bid(_))
      .find(_.validateBid(round, bids, totalPlayers).isRight)
      .getOrElse(Bid(0))
