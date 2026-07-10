package it.unibo.pps.wizard.engine.adapters

import it.unibo.pps.wizard.engine.events.InvitationEvent
import it.unibo.pps.wizard.engine.model.basic.{Bid, Card, PlayerId, Trump}
import it.unibo.pps.wizard.engine.model.view.{InvitationContextFactory, PlayCardContext}
import it.unibo.pps.wizard.engine.ports.{WizardAIPort, WizardInboundPort}
import it.unibo.pps.wizard.util.PrologEngine
import it.unibo.tuprolog.theory.Theory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Using

class WizardPrologAdapter(private val inboundPort: WizardInboundPort)
    extends WizardAIPort:
  val prologEngine = PrologEngine.buildEngine(defineTheory)

  private def defineTheory: Theory =
    import PrologEngine.given
    Using.resource(scala.io.Source.fromFile("prolog/all.pl"))(_.mkString)
  
  override def getResolvedTrumpColor(playerId: PlayerId): Future[Card.Color] = ???

  override def getPlaceBid(playerId: PlayerId): Future[Bid] = ???

  override def getBestCard(playerId: PlayerId): Future[Card] =
    inboundPort.getState.map:
      case WizardGameState.Running(state) =>
        InvitationContextFactory.fromState(state) match
          case Some(InvitationEvent.WaitingForCard(context)) if context.playerId == playerId =>
            bestCardFrom(context)
          case _ =>
            throw IllegalStateException(s"Player $playerId is not waiting for a card")
      case _ =>
        throw IllegalStateException("Game is not running")

  private def bestCardFrom(context: PlayCardContext): Card =
    import PrologEngine.given
    val goal =
      s"""best_playable_card(
         |${cardsTerm(context.hand.toList)},
         |${cardTerm(context.currentWinningCard)},
         |${colorTerm(context.followingColor)},
         |${trumpColorTerm(context.trump)},
         |${context.bid.value},
         |${context.tricksWon},
         |BestCard
         |)""".stripMargin
    prologEngine(goal)
      .find(_.isYes)
      .flatMap(solution => PrologEngine.extractVars(solution).get("BestCard"))
      .flatMap(term => context.legalCards.find(cardTerm(_) == term.toString))
      .getOrElse(context.legalCards.head)

  private def cardsTerm(cards: List[Card]): String = cards.map(cardTerm).mkString("[", ",", "]")

  private def cardTerm(card: Option[Card]): String = card.map(cardTerm).getOrElse("none")

  private def cardTerm(card: Card): String = card match
    case Card.Standard(color, rank) => s"card(${rank.value},${colorTerm(color)})"
    case _: Card.Wizard             => "wizard"
    case _: Card.Jester             => "jester"

  private def trumpColorTerm(trump: Trump): String = colorTerm(trump.effectiveColor)

  private def colorTerm(color: Option[Card.Color]): String = color.map(colorTerm).getOrElse("none")

  private def colorTerm(color: Card.Color): String = color.toString.toLowerCase
  
