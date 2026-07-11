package it.unibo.pps.wizard.engine.adapters

import it.unibo.pps.wizard.engine.model.basic.{Bid, Card, Hand, PlayerId, Table}
import it.unibo.pps.wizard.engine.model.core.GameState
import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardInboundPort}
import it.unibo.pps.wizard.engine.prolog.WizardPrologEngine
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
      case GameState.Bidding(core, _, _) =>
        withHand(core.hands.getHand(playerId)): hand =>
          engine.placeBid(hand, core.trump).getOrElse(Bid(0))

  override def adjustBid(playerId: PlayerId): Future[Bid] =
    onRunningPhase("adjust bid"):
      case GameState.Bidding(core, _, _) =>
        withHand(core.hands.getHand(playerId)): hand =>
          val rejectedBid = engine.placeBid(hand, core.trump).getOrElse(Bid(0))
          engine.adjustBid(hand, rejectedBid).getOrElse(Bid(rejectedBid.value + 1))

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
            .getOrElse(legalCards.head)
