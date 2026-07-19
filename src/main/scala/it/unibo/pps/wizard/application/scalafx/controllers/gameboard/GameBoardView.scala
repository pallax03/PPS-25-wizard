package it.unibo.pps.wizard.application.scalafx.controllers.gameboard

import it.unibo.pps.wizard.application.scalafx.util.UiPhase
import it.unibo.pps.wizard.engine.model.basic.PlayerId
import it.unibo.pps.wizard.engine.model.basic.PlayerName
import it.unibo.pps.wizard.engine.model.basic.Players
import it.unibo.pps.wizard.engine.model.basic.bidding.Bid
import it.unibo.pps.wizard.engine.model.basic.bidding.Trick
import it.unibo.pps.wizard.engine.model.basic.cards._
import it.unibo.pps.wizard.engine.model.basic.gameplay._
import it.unibo.pps.wizard.engine.model.basic.scoreboard.Scoreboard

/** Represents the view of the game board, which is responsible for displaying the game state and handling user interactions. */
trait GameBoardView:
  /**
   * Returns the ID of the current player.
   *
   * @return the PlayerId of the current player
   */
  def getCurrentPlayerId: PlayerId

  /**
   * Displays the waiting state for the trump selection by the specified player.
   *
   * @param playerId the ID of the player waiting for trump selection
   */
  def displayWaitingForTrump(playerId: PlayerId): Unit

  /**
   * Displays the waiting state for the bid placement by the specified player.
   *
   * @param winnerId the ID of the player waiting for bid placement
   */
  def displayTrickWon(winnerId: PlayerId, tricksWon: Trick, trickedCards: List[Card]): Unit

  /** Clears the game table, removing any displayed cards or information. */
  def clearTable(): Unit

  /**
   * Displays the change of phase in the game.
   *
   * @param phase the new phase of the game
   */
  def displayPhaseChanged(phase: String): Unit

  /**
   * Displays the cards dealt to the specified player, along with the trump and round information.
   *
   * @param playerId the ID of the player receiving the cards
   * @param hands the hands dealt to the player
   * @param trump the trump card for the round
   * @param round the current round number
   */
  def displayCardsDealt(playerId: PlayerId, hands: Hands, trump: Trump, round: Round): Unit

  /**
   * Displays the card played by a player, along with the winning card and following color information.
   *
   * @param playerId the ID of the player who played the card
   * @param playerName the name of the player who played the card
   * @param card the card played by the player
   * @param winningCard an optional winning card for the trick
   * @param followingColor an optional following color for the trick
   */
  def displayCardPlayed(
      playerId: PlayerId,
      playerName: PlayerName,
      card: Card,
      winningCard: Option[Card],
      followingColor: Option[Card.Color]
  ): Unit

  /**
   * Displays the trump color selected by a player.
   *
   * @param playerId the ID of the player who selected the trump color
   * @param color the selected trump color
   */
  def displayTrumpSelected(playerId: PlayerId, color: Card.Color): Unit

  /**
   * Displays the bid placed by a player.
   *
   * @param playerId the ID of the player who placed the bid
   * @param bid the bid placed by the player
   */
  def displayBidPlaced(playerId: PlayerId, bid: Bid): Unit

  /**
   * Displays the change of turn in the game, indicating the current player and phase.
   *
   * @param playerId the ID of the player whose turn has changed
   * @param phase the new phase of the game
   */
  def displayTurnChanged(playerId: PlayerId, phase: UiPhase): Unit

  /**
   * Displays the score for the round, including the scoreboard and player information.
   *
   * @param scoreboard the scoreboard containing the scores for the round
   * @param players the players participating in the game
   */
  def displayRoundScored(scoreboard: Scoreboard, players: Players): Unit

  /**
   * Displays the start of the game, including the players participating in the game.
   *
   * @param players the players participating in the game
   */
  def displayGameStarted(players: Players): Unit

  /**
   * Displays the end of the game, including the final scoreboard and player information.
   *
   * @param scoreboard the final scoreboard of the game
   * @param players the players who participated in the game
   */
  def displayGameEnded(scoreboard: Scoreboard, players: Players): Unit

  /**
   * Displays the legal cards that a player can play.
   *
   * @param playerId the ID of the player whose legal cards are being displayed
   * @param legalCards the list of legal cards that the player can play
   */
  def displayLegalCards(playerId: PlayerId, legalCards: List[Card]): Unit

  /**
   * Displays an error message to the user.
   *
   * @param message the error message to be displayed
   */
  def displayErrorMessage(message: String): Unit

  /** Displays an invalid bid message to the user. */
  def displayShowInvalidBid(): Unit

  /** Clears the invalid bid message from the user interface. */
  def displayClearInvalidBid(): Unit
