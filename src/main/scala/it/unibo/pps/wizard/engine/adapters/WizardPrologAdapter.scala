package it.unibo.pps.wizard.engine.adapters

import it.unibo.pps.wizard.engine.model.basic.{Bid, Bids, Card, Hand, PlayerId, Round, Table}
import it.unibo.pps.wizard.engine.model.core.GameState
import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardInboundPort}
import it.unibo.pps.wizard.engine.prolog.WizardPrologEngine
import it.unibo.pps.wizard.engine.model.rules.BiddingRules.*
import it.unibo.pps.wizard.engine.model.rules.TableRules.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class WizardPrologAdapter(private val inboundPort: WizardInboundPort) extends WizardAIPort:

  private val engine = WizardPrologEngine()

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

  private def withHand[T](handOpt: Option[Hand])(prologLogic: Hand => T): Future[T] =
    handOpt match
      case Some(hand) => Future.successful(prologLogic(hand))
      case None       => Future.failed(IllegalArgumentException("Player not found in game state"))

  override def resolvedTrumpColor(playerId: PlayerId): Future[Card.Color] =
    onRunningPhase("choose trump color"):
      case GameState.ChoosingTrump(core) =>
        withHand(core.hands.getHand(playerId)): hand =>
          engine.chooseTrumpColor(hand).getOrElse(Card.Color.values.head)

  override def placeBid(playerId: PlayerId): Future[Bid] =
    onRunningPhase("place bid"):
      case GameState.Bidding(core, currentBids, _) =>
        withHand(core.hands.getHand(playerId)): hand =>
          val isValid = (bid: Bid) =>
            bid.validateBid(core.round, currentBids, core.players.totalPlayers).isRight
          val proposedBid = engine.placeBid(hand, core.trump)
          proposedBid
            .filter(isValid)
            .orElse(proposedBid.flatMap(engine.adjustBid(hand, _)).filter(isValid))
            .getOrElse(firstValidBid(core.round, currentBids, core.players.totalPlayers))

  override def adjustBid(playerId: PlayerId): Future[Bid] =
    onRunningPhase("adjust bid"):
      case GameState.Bidding(core, currentBids, _) =>
        withHand(core.hands.getHand(playerId)): hand =>
          val rejectedBid = Bid(core.round.value - currentBids.total.value)
          engine
            .adjustBid(hand, rejectedBid)
            .filter(_.validateBid(core.round, currentBids, core.players.totalPlayers).isRight)
            .getOrElse(firstValidBid(core.round, currentBids, core.players.totalPlayers))

  override def bestCard(playerId: PlayerId): Future[Card] =
    onRunningPhase("play best card"):
      case GameState.Playing(core, bids, table, _, tricks) =>
        withHand(core.hands.getHand(playerId)): hand =>
          val legalCards = hand.legalCards(table)
          engine
            .bestPlayableCard(
              hand = hand,
              winningCard = table.evaluateTrick(core.trump),
              followingColor = table.followingCard.collect { case Card.Standard(c, _) => c },
              trump = core.trump,
              playerBid = bids(playerId),
              playerTrick = Bid(tricks(playerId)) // TODO: Bid and Trick need to be refactored
            )
            .filter(legalCards.contains)
            .getOrElse(legalCards.head)

  private def firstValidBid(round: Round, bids: Bids, totalPlayers: Int): Bid =
    (0 to round.value)
      .map(Bid(_))
      .find(_.validateBid(round, bids, totalPlayers).isRight)
      .getOrElse(Bid.zero)
