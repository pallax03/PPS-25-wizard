package it.unibo.pps.wizard.engine.model.core

import GameError.*
import it.unibo.pps.wizard.engine.model.basic.{Deck, Hands, PlayerId, Players, Round, Scoreboard}

object GameEngine:
  def processAction(state: GameState, action: GameAction): Either[GameError, GameState] =
    (state, action) match
      case (GameState.Bidding, GameAction.PlaceBid)    => ???
      case (GameState.Bidding, GameAction.ChooseTrump) => ???

      case (GameState.Playing, GameAction.PlayCard) => ???

      case (GameState.Ended, _) => ???
      case (_, _)               => Left(InvalidAction)

  def initializeGame(players: Players): GameState =
    val core = CoreState(
      players = players,
      hands = Hands.empty,
      deck = Deck.create,
      round = Round.start,
      dealerId = PlayerId(0),
      scoreboard = Scoreboard.empty
    )

    GameState.Ended(Scoreboard.empty)
