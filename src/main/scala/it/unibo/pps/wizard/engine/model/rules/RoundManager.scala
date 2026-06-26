package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.GameError

trait RoundManager:
    def nextPlayer(current: PlayerId, players: List[Player]): Either[GameError, PlayerId]
    def firstPlayerOfRound(roundNumber: Int, players: List[Player]): PlayerId

    def dealCards(deck: Deck, roundNumber: Int, players: List[Player]): (Map[PlayerId, Hand], Deck, Option[Card])

    def validateBiddingTurn(actionPlayer: PlayerId, currentPlayerTurn: PlayerId): Either[GameError, Unit]
    def isBiddingPhaseComplete(bids: BidsCollection, totalPlayers: Int): Boolean

    def validatePlayingTurn(actionPlayer: PlayerId, currentPlayerTurn: PlayerId): Either[GameError, Unit]
    def isTrickComplete(table: Table, totalPlayers: Int): Boolean
    def isRoundComplete(currentTrickNumber: Int, totalTricksForRound: Int): Boolean

object RoundManager:
  def apply(): RoundManager = new StandardRoundManager

  private class StandardRoundManager extends RoundManager:
    override def validateBiddingTurn(actionPlayer: PlayerId, currentPlayerTurn: PlayerId): Either[GameError, Unit] =
      if actionPlayer == currentPlayerTurn then Right(())
      else Left(GameError.NotYourTurn)

    override def isBiddingPhaseComplete(bids: BidsCollection, totalPlayers: Int): Boolean =
      bids.size == totalPlayers

    override def nextPlayer(current: PlayerId, players: List[Player]): Either[GameError, PlayerId] =
      val index = players.indexWhere(_.id == current)
      if index == -1 then
        Left(GameError.NotYourTurn)
      else
        Right(players((index + 1) % players.size).id)

    override def firstPlayerOfRound(roundNumber: Int, players: List[Player]): PlayerId = ???

    override def dealCards(deck: Deck, roundNumber: Int, players: List[Player]): (Map[PlayerId, Hand], Deck, Option[Card]) = ???

    override def validatePlayingTurn(actionPlayer: PlayerId, currentPlayerTurn: PlayerId): Either[GameError, Unit] = ???

    override def isTrickComplete(table: Table, totalPlayers: Int): Boolean = ???

    override def isRoundComplete(currentTrickNumber: Int, totalTricksForRound: Int): Boolean = ???