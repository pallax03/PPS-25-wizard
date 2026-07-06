package it.unibo.pps.wizard.engine.model.rules

import cats.data.State
import it.unibo.pps.wizard.engine.model.basic.*
import it.unibo.pps.wizard.engine.model.core.{CoreState, GameError, GameState}

object RoundManager:

  extension (players: Players)
    def nextAfter(current: PlayerId): Either[GameError, PlayerId] =
      val idx = players.toList.indexWhere(_.id == current)
      Either.cond(
        idx >= 0,
        players.toList((idx + 1) % players.toList.size).id,
        GameError.NotYourTurn
      )

  extension (round: Round)
    def firstPlayer(players: Players): PlayerId =
      players.toList((round.value - 1) % players.toList.size).id

    def isComplete(currentTrickCount: Int): Boolean =
      currentTrickCount == round.value

    def deal(players: Players): State[Deck, (Hands, Option[Card])] =
      val cardsPerPlayer = round.value
      for
        drawn <- Deck.pop(cardsPerPlayer * players.toList.size)
        hands = Hands(
          players.toList.map(_.id).zip(drawn.grouped(cardsPerPlayer).map(Hand(_)).toList).toMap
        )
        currentDeck <- State.get[Deck]
        trump <-
          if currentDeck.length > 0 then Deck.pop(1).map(_.headOption)
          else State.pure[Deck, Option[Card]](None)
      yield (hands, trump)

    def initialize: State[CoreState, GameState.Bidding] =
      for
        core <- State.get[CoreState]

        (remainingDeck, (hands, maybeTrump)) = round.deal(core.players).run(core.deck).value

        firstPlayer = round.firstPlayer(core.players)

        newCore = core.copy(
          hands = hands,
          deck = remainingDeck
        )

        _ <- State.set(newCore)
      yield GameState.Bidding(
        core = newCore,
        trump = Trump.asTrump(maybeTrump),
        currentBids = Bids.empty,
        currentPlayer = firstPlayer
      )

  extension (expectedPlayer: PlayerId)
    def validateTurnOf(actionPlayer: PlayerId): Either[GameError, Unit] =
      Either.cond(actionPlayer == expectedPlayer, (), GameError.NotYourTurn)

  extension (bidsCount: Int)
    def isBiddingPhaseComplete(totalPlayers: Int): Boolean =
      bidsCount == totalPlayers

  extension (table: Table)
    def isTrickComplete(totalPlayers: Int): Boolean =
      table.size == totalPlayers

export RoundManager.*
