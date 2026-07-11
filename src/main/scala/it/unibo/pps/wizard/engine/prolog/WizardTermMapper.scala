package it.unibo.pps.wizard.engine.prolog

import it.unibo.pps.wizard.engine.model.basic.*

object WizardTermMapper:
  private final val NO_VALUE: String = "none"

  def cardsTerm(cards: List[Card]): String = cards.map(c)
  def cardTerm(card: Option[Card]): String = card.map(cardTerm).getOrElse(NO_VALUE)
  def cardTerm(card: Card): String = card match
    case card: SpecialCard => card.getClass.getSimpleName.toLowerCase
    case Card.Standard(color, rank) => s"card(${rank.value},${colorTerm(color)})"

