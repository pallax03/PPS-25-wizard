package it.unibo.pps.wizard.engine.prolog

import it.unibo.pps.wizard.engine.model.basic.{Bid, Trick}
import it.unibo.pps.wizard.engine.model.basic.cards.{Card, Hand}
import it.unibo.pps.wizard.engine.model.basic.gameplay.Trump

import it.unibo.pps.wizard.engine.prolog.WizardTermMapper.*

import it.unibo.pps.wizard.util.PrologEngine
import alice.tuprolog.{Term, Theory}

import scala.util.Using

class WizardPrologEngine:
  private val prologEngine = PrologEngine.buildEngine(defineTheory)

  private def defineTheory: Theory =
    import PrologEngine.given
    Using.resource(scala.io.Source.fromFile("prolog/all.pl"))(_.mkString)

  def chooseTrumpColor(hand: Hand): Option[Card.Color] =
    query(s"choose_trump(${cardsTerm(hand.toList)}, TrumpColor)", "TrumpColor").flatMap(term =>
      Card.Color.values.find(colorTerm(_) == term.toString)
    )

  def placeBid(hand: Hand, trump: Trump): Option[Bid] =
    query(s"place_bid(${cardsTerm(hand.toList)}, ${trumpColorTerm(trump)}, Bid)", "Bid").map(term =>
      Bid(term.toString.toInt)
    )

  def adjustBid(hand: Hand, rejectedBid: Bid): Option[Bid] =
    query(s"adjust_bid(${cardsTerm(hand.toList)}, $rejectedBid, FinalBid)", "FinalBid").map(term =>
      Bid(term.toString.toInt)
    )

  def bestPlayableCard(
      hand: Hand,
      winningCard: Option[Card],
      followingColor: Option[Card.Color],
      trump: Trump,
      playerBid: Bid,
      playerTrick: Trick
  ): Option[Card] = query(
    s"""best_playable_card(
             |${cardsTerm(hand.toList)},
             |${cardTerm(winningCard)},
             |${colorTerm(followingColor)},
             |${trumpColorTerm(trump)},
             |$playerBid,
             |$playerTrick,
             |BestCard
             |)""".stripMargin,
    "BestCard"
  ).flatMap(term => hand.toList.find(cardTerm(_) == term.toString))

  private def query[B](goal: String, extractTerm: String): Option[Term] =
    prologEngine(goal)
      .find(_.isSuccess)
      .flatMap(solution => PrologEngine.extractVars(solution).get(extractTerm))
