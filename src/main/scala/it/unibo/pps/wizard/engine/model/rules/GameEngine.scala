package it.unibo.pps.wizard.engine.model.rules

import it.unibo.pps.wizard.engine.model.rules.GameError.*

trait GameEngine:
  def processAction(state: GameState, action: GameAction): Either[GameError, GameState] = (state, action) match
    case (GameState.Dealing, _) => ???
    case (GameState.Bidding, GameAction.PlaceBid) => ???
    case (GameState.Bidding, GameAction.ChooseTrump) => ???
    case (GameState.Playing, GameAction.PlayCard) => ???
    case (GameState.Scoring, _) => ???
    case (_, _) => Left(InvalidAction)