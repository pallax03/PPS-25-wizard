package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameError
import it.unibo.pps.wizard.engine.model.core.Reasons.{CardNotInHand, MustFollowLeader}

object TableRules:

  extension (h: Hand)
    private def hasColor(color: Card.Color): Boolean = h.toList.exists:
      case Card.Standard(c, _) => c == color
      case _                   => false

  extension (cardPlayed: Card)
    def validateAgainst(table: Table, hand: Hand): Either[GameError, Unit] =
      if !hand.contains(cardPlayed) then return Left(GameError.CardNotAllowed(CardNotInHand))

      cardPlayed match
        case _: SpecialCard => Right(())
        case Card.Standard(playedColor, _) =>
          table.followingCard match
            case None => Right(())
            case Some(Card.Standard(leaderColor, _)) =>
              if playedColor == leaderColor then Right(())
              else if hand.hasColor(leaderColor) then
                Left(GameError.CardNotAllowed(MustFollowLeader(leaderColor)))
              else Right(())

  extension (table: Table)
    def evaluateTrickWinner(trump: Trump): PlayerId =
      val cards = table.playedCards
      val trumpColor = trump.effectiveColor
      val followingColor = table.followingCard.map(_.color)

      def highestOf(targetColor: Option[Card.Color]): Option[Card] =
        cards
          .collect { case c @ Card.Standard(color, rank) if targetColor.contains(color) => c }
          .maxByOption(_.rank.value)

      val winningCard = cards
        .find(_.isInstanceOf[Card.Wizard])
        .orElse(highestOf(trumpColor))
        .orElse(highestOf(followingColor))
        .getOrElse(cards.head)

      table.playerOf(winningCard).get
