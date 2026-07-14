package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameError
import it.unibo.pps.wizard.engine.model.core.CardNotAllowedReasons.*

object TableRules:

  extension (h: Hand)
    private def hasColor(color: Card.Color): Boolean = h.toList.exists:
      case Card.Standard(c, _) => c == color
      case _                   => false
    def legalCards(table: Table): List[Card] = h.toList.filter(_.isLegal(table, h))

  extension (cardPlayed: Card)
    private def isLegal(table: Table, hand: Hand): Boolean =
      if !hand.contains(cardPlayed) then false
      else
        cardPlayed match
          case _: SpecialCard => true
          case Card.Standard(playedColor, _) =>
            table.followingColor match
              case Some(followingColor) =>
                playedColor == followingColor || !hand.hasColor(followingColor)
              case _ => true

    def validateAgainst(table: Table, hand: Hand): Either[GameError, Unit] =
      if !hand.contains(cardPlayed) then
        Left(GameError.CardNotAllowed(CardNotInHand(hand.legalCards(table))))
      else
        cardPlayed match
          case _: SpecialCard => Right(())
          case Card.Standard(playedColor, _) =>
            table.followingColor match
              case Some(followingColor) if playedColor != followingColor =>
                if hand.hasColor(followingColor) then
                  Left(
                    GameError.CardNotAllowed(
                      MustFollowColor(followingColor, hand.legalCards(table))
                    )
                  )
                else Right(())
              case _ => Right(())

  extension (table: Table)
    def evaluateTrick(trump: Trump): Option[Card] =
      val cards = table.playedCards
      val trumpColor = trump.effectiveColor
      val followingColor = table.followingColor

      def highestOf(targetColor: Option[Card.Color]): Option[Card] =
        cards
          .collect { case c @ Card.Standard(color, rank) if targetColor.contains(color) => c }
          .maxByOption(_.rank.value)

      cards
        .find(_.isInstanceOf[Card.Wizard])
        .orElse(highestOf(trumpColor))
        .orElse(highestOf(followingColor))
        .orElse(cards.headOption)

export TableRules.*
