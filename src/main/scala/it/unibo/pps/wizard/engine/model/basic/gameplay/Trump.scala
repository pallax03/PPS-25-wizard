package it.unibo.pps.wizard.engine.model.basic.gameplay

import it.unibo.pps.wizard.engine.model.basic.cards.Card
import it.unibo.pps.wizard.engine.model.core.GameError

enum Trump:
  case Absent
  case Jester(c: Card.Jester)
  case Standard(c: Card.Standard)
  case WizardUnresolved(c: Card.Wizard)
  case WizardResolved(c: Card.Wizard, color: Card.Color)

  def effectiveColor: Option[Card.Color] = this match
    case Standard(c)              => Some(c.color)
    case WizardResolved(_, color) => Some(color)
    case _                        => None

  def card: Option[Card] = this match
    case Absent               => None
    case Jester(c)            => Some(c)
    case Standard(c)          => Some(c)
    case WizardUnresolved(c)  => Some(c)
    case WizardResolved(c, _) => Some(c)

object Trump:
  def apply(c: Card): Trump = c match
    case j: Card.Jester   => Trump.Jester(j)
    case w: Card.Wizard   => Trump.WizardUnresolved(w)
    case s: Card.Standard => Trump.Standard(s)

  extension (t: Trump)
    infix def resolveWizard(color: Card.Color): Either[GameError, Trump] = t match
      case Trump.WizardUnresolved(c) => Right(Trump.WizardResolved(c, color))
      case _                         => Left(GameError.InvalidAction)
