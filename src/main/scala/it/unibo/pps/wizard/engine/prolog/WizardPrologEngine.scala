package it.unibo.pps.wizard.engine.prolog

import it.unibo.pps.wizard.engine.model.basic.{Bid, Card, Hand, Trump}
import it.unibo.pps.wizard.engine.model.basic.Card.Color
import it.unibo.pps.wizard.engine.prolog.WizardTermMapper.*
import it.unibo.pps.wizard.util.PrologEngine
import it.unibo.tuprolog.core.Term
import it.unibo.tuprolog.theory.Theory

import scala.util.Using

class WizardPrologEngine:
  private val prologEngine = PrologEngine.buildEngine(defineTheory)

  private def defineTheory: Theory =
    import PrologEngine.given
    Seq(
      "prolog/utils.pl",
      "prolog/engine_basic.pl",
      "prolog/engine_rules.pl",
      "prolog/strategy_helper.pl",
      "prolog/strategy.pl",
      "prolog/api.pl"
    ).map(f => Using.resource(scala.io.Source.fromFile(f))(_.mkString)).mkString("\n")

  def chooseTrumpColor(hand: Hand): Option[Color] =
    query(s"choose_trump(${cardsTerm(hand.toList)}, TrumpColor)", "TrumpColor").flatMap(term =>
      Card.Color.values.find(colorTerm(_) == term.toString)
    )

  def placeBid(hand: Hand, trump: Trump): Option[Bid] =
    query(s"place_bid(${cardsTerm(hand.toList)}, ${trumpColorTerm(trump)}, Bid)", "Bid").map(term =>
      Bid(term.toString.toInt)
    )

  def adjustBid(hand: Hand, rejectedBid: Bid): Option[Bid] =
    query(s"adjust_bid(${cardsTerm(hand.toList)}, ${rejectedBid.value}, FinalBid)", "FinalBid").map(
      term => Bid(term.toString.toInt)
    )

  def bestPlayableCard(
      hand: Hand,
      winningCard: Card,
      followingColor: Color,
      trump: Trump,
      playerBid: Bid,
      playerTrick: Bid
  ): Option[Card] = query(
    s"""best_playable_card(
             |${cardsTerm(hand.toList)},
             |${cardTerm(winningCard)},
             |${colorTerm(followingColor)},
             |${trumpColorTerm(trump)},
             |${playerBid.value},
             |${playerTrick.value},
             |BestCard
             |)""".stripMargin,
    "BestCard"
  ).flatMap(term => hand.toList.find(cardTerm(_) == term.toString))

  private def query[B](goal: String, extractTerm: String): Option[Term] =
    import PrologEngine.given
    prologEngine(goal)
      .find(_.isYes)
      .flatMap(solution => PrologEngine.extractVars(solution).get(extractTerm))
