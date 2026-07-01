package it.unibo.pps.wizard.engine.model.basic

opaque type Table = List[(PlayerId, Card)]
object Table:
  def empty: Table = List.empty

  extension (p: PlayerId)
    infix def plays(c: Card): (PlayerId, Card) = (p, c)

  extension (t: Table)
    def isEmpty: Boolean = t.isEmpty
    def size: Int = t.length

    def plays: List[(PlayerId, Card)] = t
    def playedCards: List[Card] = t.map(_._2)
    def playerOf(card: Card): Option[PlayerId] = t.find(_._2 == card).map(_._1)

    def followingCard: Option[Card.Standard] =
      t.playedCards
        .dropWhile(_.isInstanceOf[Card.Jester])
        .headOption
        .collect { case s: Card.Standard => s }

    infix def +(play: (PlayerId, Card)): Table = t :+ play
//    todo: removed from +, for testing gui: private[model]

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
    case Absent => None
    case Jester(c) => Some(c)
    case Standard(c) => Some(c)
    case WizardUnresolved(c) => Some(c)
    case WizardResolved(c, _) => Some(c)
object Trump:
  def apply(c: Card): Trump = c match
    case j: Card.Jester   => Trump.Jester(j)
    case w: Card.Wizard   => Trump.WizardUnresolved(w)
    case s: Card.Standard => Trump.Standard(s)

  extension (optCard: Option[Card])
    def asTrump: Trump = optCard match
      case Some(card) => Trump(card)
      case None => Trump.Absent

  extension (t: Trump.WizardUnresolved)
    infix def resolvedAs(color: Card.Color): Trump.WizardResolved =
      Trump.WizardResolved(t.c, color)

opaque type Round = Int
object Round:
  def start: Round = 1

  extension (r: Round)
    def value: Int = r
    def next: Round = r + 1