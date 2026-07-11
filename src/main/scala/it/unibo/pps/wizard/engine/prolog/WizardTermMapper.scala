package it.unibo.pps.wizard.engine.prolog

import it.unibo.pps.wizard.engine.model.basic.*

object WizardTermMapper:
  final val NO_VALUE: String = "none"

  def cardsTerm(cards: List[Card]): String = cards.map(cardTerm).mkString("[",",","]")
  
  def cardTerm(card: Option[Card]): String = card.map(cardTerm).getOrElse(NO_VALUE)
  
  def cardTerm(card: Card): String = card match
    case card: SpecialCard => card.getClass.getSimpleName.toLowerCase
    case Card.Standard(color, rank) => s"card(${rank.value},${colorTerm(color)})"

  def trumpColorTerm(trump: Trump): String = colorTerm(trump.effectiveColor)
  
  def colorTerm(color: Option[Card.Color]): String = color.map(colorTerm).getOrElse(NO_VALUE)
  
  def colorTerm(color: Card.Color): String = color.toString.toLowerCase

