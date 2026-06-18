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