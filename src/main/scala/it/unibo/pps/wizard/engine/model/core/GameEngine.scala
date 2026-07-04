package it.unibo.pps.wizard.engine.model.core

import GameError.*
import it.unibo.pps.wizard.engine.model.basic.{Deck, Hands, PlayerId, Players, Round, Scoreboard}
import it.unibo.pps.wizard.engine.model.rules.BiddingRules
import it.unibo.pps.wizard.engine.model.rules.RoundManager.*

object GameEngine:
  def processAction(state: GameState, action: GameAction): Either[GameError, GameState] =
    (state, action) match
      case (currentState: GameState.Bidding, GameAction.PlaceBid(playerId, bid)) =>
        if playerId == currentState.currentPlayer then
          BiddingRules.processBid(
            bid,
            currentState.currentBids,
            playerId,
            currentState.core.round,
            currentState.core.players.toList.size
          ) match
            case Left(error) => Left(error)
            case Right(bids) =>
              Right(
                GameState.Bidding(
                  core = currentState.core,
                  trump = currentState.trump,
                  currentBids = bids,
                  currentPlayer = currentState.core.players
                    .nextAfter(playerId)
                    .getOrElse(currentState.currentPlayer)
                )
              )
        else Left(NotYourTurn)

      case (GameState.Bidding, GameAction.ChooseTrump) => ???

      case (GameState.Playing, GameAction.PlayCard) => ???

      case (GameState.Ended, _) => ???
      case (_, _)               => Left(InvalidAction)

  def initializeGame(players: Players): GameState =
    val round = Round.start
    val core = CoreState(
      players = players,
      hands = Hands.empty,
      deck = Deck.create,
      round = round,
      dealerId = PlayerId(0),
      scoreboard = Scoreboard.empty
    )
    val (_, biddingState) = round.initialize.run(core).value
    biddingState
