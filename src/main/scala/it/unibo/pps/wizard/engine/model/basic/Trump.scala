package it.unibo.pps.wizard.engine.model.basic

enum Trump:
  case Absent
  case Jester(card: Card)
  case Standard(card: Card.Standard)
  case WizardUnresolved(card: Card)
  case WizardResolved(card: Card, color: Card.Color)

  def effectiveColor: Option[Card.Color] = this match
    case Standard(c)              => Some(c.color)
    case WizardResolved(_, color) => Some(color)
    case _                        => None

  def getCard: Option[Card] = this match
    case Trump.Absent => None
    case Trump.Jester(card) => Some(card)
    case Trump.Standard(card) => Some(card)
    case Trump.WizardUnresolved(card) => Some(card)
    case Trump.WizardResolved(card, color) => Some(card)


object Trump:
  def apply(c: Card): Trump = c match
    case j: Card.Jester => Trump.Jester(j)
    case w: Card.Wizard => Trump.WizardUnresolved(w)
    case s: Card.Standard => Trump.Standard(s)