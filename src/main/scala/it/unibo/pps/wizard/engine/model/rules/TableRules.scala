package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameError


trait TableRules:
  def validateCard(table: Table, hand: Hand, cardPlayed: Card, trump: Trump): Either[GameError, Unit]
  def evaluateTrickWinner(table: Table, trump: Trump): PlayerId

object TableRules:
  def apply(): TableRules = new StandardWizardTableRules

  private class StandardWizardTableRules extends TableRules:

    override def validateCard(table: Table, hand: Hand, cardPlayed: Card, trump: Trump): Either[GameError, Unit] =
      if !hand.contains(cardPlayed) then return Left(GameError.CardNotAllowed)
      val leaderColorOption = table.leaderCard.map(_.color)
      (leaderColorOption, cardPlayed) match
        case (Some(leaderColor), Card.Standard(c, _)) if c != leaderColor =>
          val hasLeaderColor = hand.toList.exists:
            case Card.Standard(color, _) => color == leaderColor
            case _ => false
          if hasLeaderColor then Left(GameError.CardNotAllowed) else Right(())
        case _ => Right(())

    override def evaluateTrickWinner(table: Table, trump: Trump): PlayerId =
      val cards = table.playedCards
      val trumpColor = trump.effectiveColor
      val leaderColor: Option[Color] = table.leaderCard.map(_.color)

      def highestOf(targetColor: Option[Card.Color]): Option[Card] =
        cards.collect:
          case c@Card.Standard(color, _) if targetColor.contains(color) => c
        .maxByOption(_.rank.ordinal)
      
      val winningCard = cards.find(_.isInstanceOf[Card.Wizard])
        .orElse(highestOf(trumpColor))
        .orElse(highestOf(leaderColor))
        .getOrElse(cards.head)

      table.playerOf(winningCard).get