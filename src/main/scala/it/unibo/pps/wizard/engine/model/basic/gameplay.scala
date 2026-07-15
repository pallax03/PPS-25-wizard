package it.unibo.pps.wizard.engine.model.basic

import it.unibo.pps.wizard.engine.model.core.GameError

opaque type Table = List[(PlayerId, Card)]

object Table:
  def empty: Table = List.empty

  extension (t: Table)
    def isEmpty: Boolean = t.isEmpty
    def size: Int = t.length
    def isTrickComplete(totalPlayers: Int): Boolean = t.size == totalPlayers

    def plays: List[(PlayerId, Card)] = t
    def playedCards: List[Card] = t.map(_._2)
    def playerOf(card: Card): Option[PlayerId] = t.find(_._2 == card).map(_._1)

    def followingColor: Option[Card.Color] =
      if t.playedCards.exists(c => c.isInstanceOf[Card.Wizard]) then Option.empty
      else
        t.playedCards
          .dropWhile(_.isInstanceOf[Card.Jester])
          .headOption
          .collect { case s: Card.Standard => s.color }

    infix def +(play: (PlayerId, Card)): Table = t :+ play

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

//  extension (t: Trump.WizardUnresolved) infix def resolvedAs(color: Card.Color): Trump = Trump.WizardResolved(t.c, color)

  extension (t: Trump)
    infix def resolveWizard(color: Card.Color): Either[GameError, Trump] = t match
      case Trump.WizardUnresolved(c) => Right(Trump.WizardResolved(c, color))
      case _                         => Left(GameError.InvalidAction)

opaque type Round = Int

object Round:
  def start: Round = 1
  def apply(value: Int): Round = value

  extension (r: Round)
    def value: Int = r
    def next: Round = r + 1
