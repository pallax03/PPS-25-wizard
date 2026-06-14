package it.unibo.pps.wizard.model

object BiddingManager:
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

  private def isYourTurn(state: GameState.Bidding, playerId: PlayerId): Boolean =
    state.currentPlayer == playerId

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

  private def isBiddingPhaseComplete(bids: BidsCollection, players: List[Player]): Boolean =
    bids.size == players.size

  private def nextPlayer(current: PlayerId, players: List[Player]): PlayerId =
    val index = players.indexWhere(_.id == current)
    players((index + 1) % players.size).id

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