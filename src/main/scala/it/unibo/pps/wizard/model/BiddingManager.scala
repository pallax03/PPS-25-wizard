package it.unibo.pps.wizard.model

/**
 * Manager responsible for handling the bidding phase of a round.
 * It validates individual player bids sequentially, applies specialized rules such as
 * the Canadian Variant (Hook rule), and automates the transition to the playing phase
 * once all bids have been collected.
 */
object BiddingManager:

  /**
   * Processes a incoming bidding action from a player. Validates the turn order and bid values,
   * updates the current collection of bids, and advances the game state.
   *
   * @param state  The current [[GameState.Bidding]] containing the ongoing bids and active player turn.
   * @param action The [[GameAction.PlaceBid]] detailing which player is making the bid and its value.
   * @return An [[Either]] containing a [[GameError]] if validation fails, or the updated [[GameState]]
   *         (either the next Bidding turn or transitioning to [[GameState.Playing]]).
   */
  def processBid(state: GameState.Bidding, action: GameAction.PlaceBid): Either[GameError, GameState] =
    val playerId = action.playerId
    val bid = action.bid

    if !isYourTurn(state, playerId) then
      Left(GameError.NotYourTurn)
    else if !isBidValid(state, bid) then
      Left(GameError.InvalidBid)
    else
      val updatedBids = state.currentBids + (playerId -> bid)

      if isBiddingPhaseComplete(updatedBids, state.core.players) then
        Right(transitionToPlaying(state, updatedBids))
      else
        Right(state.copy(
          currentBids = updatedBids,
          currentPlayer = nextPlayer(playerId, state.core.players)
        ))

  /**
   * Verifies if the bidding action is being performed by the player whose turn it currently is.
   */
  private def isYourTurn(state: GameState.Bidding, playerId: PlayerId): Boolean =
    state.currentPlayer == playerId

  /**
   * Validates the legal numeric boundaries of a bid based on the current round context,
   * including enforcing the "Canadian Variant" (Hook rule) restriction on the last bidder.
   *
   * Rules enforced:
   * 1. A bid must be greater than or equal to 0 and cannot exceed the total number of cards dealt in the current round.
   * 2. The final player to bid (the dealer) cannot choose a value that, when added to all previous bids,
   * equals the total number of tricks available (the round number), thereby preventing a perfect tie.
   */
  private def isBidValid(state: GameState.Bidding, bid: Bid): Boolean =
    val bidValue = bid.toInt
    val roundValue = state.core.round.toInt

    val isWithinBounds = bidValue >= 0 && bidValue <= roundValue

    val isLastPlayer = state.currentBids.size == state.core.players.size - 1

    val isHookValid = if isLastPlayer then
      val underlyingBids = state.currentBids.asInstanceOf[Map[PlayerId, Bid]]
      val previousBidsSum = underlyingBids.values.map(_.toInt).sum

      (previousBidsSum + bidValue) != roundValue
    else
      true

    isWithinBounds && isHookValid

  /**
   * Checks whether every active player in the match has successfully submitted a bid.
   */
  private def isBiddingPhaseComplete(bids: BidsCollection, players: List[Player]): Boolean =
    bids.size == players.size

  /**
   * Computes the next player ID in a sequential clockwise order based on the current active list.
   */
  private def nextPlayer(current: PlayerId, players: List[Player]): PlayerId =
    val index = players.indexWhere(_.id == current)
    players((index + 1) % players.size).id

  /**
   * Handles the inner structural conversion from the Bidding phase into the Playing phase.
   * Initializes the shared trick table as empty, establishes the starting turns to the player
   * sitting immediately clockwise after the dealer, and zeroes out the trick tracking counters.
   */
  private def transitionToPlaying(state: GameState.Bidding, finalBids: BidsCollection): GameState.Playing =
    val firstPlayer = nextPlayer(state.core.dealerId, state.core.players)
    GameState.Playing(
      core = state.core,
      trump = state.trump,
      bids = finalBids,
      table = Table.empty,
      currentPlayerTurn = firstPlayer,
      tricksWon = TricksWon.initialize(state.core.players),
      leadPlayer = firstPlayer
    )