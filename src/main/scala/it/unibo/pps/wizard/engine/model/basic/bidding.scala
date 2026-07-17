package it.unibo.pps.wizard.engine.model.basic

import it.unibo.pps.wizard.engine.model.basic.gameplay.Round

/** Represents the bid (predicted number of tricks to win) placed by a player for a round. */
type Bid = Int

object Bid:
  def apply(value: Int): Bid = value

  extension (b: Bid)
    /**
     * Checks if the bid is valid for the given round.
     * A bid is valid if it does not exceed the total number of cards dealt in that round.
     *
     * @param round the current game round.
     * @return true if the bid is less than or equal to the round number, false otherwise.
     */
    def isValid(round: Round): Boolean = b <= round.value

/** Represents the collection of bids placed by all players in a round. */
opaque type Bids = Map[PlayerId, Bid]

object Bids:
  def empty: Bids = Map.empty

  extension (b: Bids)
    /**
     * Returns the bid placed by a specific player, defaulting to 0 if not found.
     *
     * @param p the player ID.
     * @return the player's bid.
     */
    def apply(p: PlayerId): Bid = b.getOrElse(p, 0)

    /**
     * Adds or updates a player's bid in the collection.
     *
     * @param entry a tuple associating a player ID with their bid.
     * @return the updated [[Bids]] collection.
     */
    infix def +(entry: (PlayerId, Bid)): Bids = b + entry

    /**
     * Checks if all players have placed their bids.
     *
     * @param totalPlayers the total number of players in the game.
     * @return true if the number of recorded bids matches the player count.
     */
    def isComplete(totalPlayers: Int): Boolean = b.size == totalPlayers

    /**
     * Calculates the sum of all bids placed in the current round.
     *
     * @return the total number of bids.
     */
    def total: Bid = b.values.sum

/** Represents the number of tricks won by a player. */
type Trick = Int

object Trick:
  def apply(value: Int): Trick = value

  extension (t: Trick) def value: Int = t

/** Represents the count of won tricks for each player in the current round. */
opaque type Tricks = Map[PlayerId, Trick]

object Tricks:
  /**
   * Initializes the trick count to 0 for all active players.
   *
   * @param players the list of players.
   * @return a [[Tricks]] map with all player scores set to 0.
   */
  def initialize(players: Players): Tricks =
    players.toList.map(_.id -> 0).toMap
  def empty: Tricks = Map.empty

  extension (t: Tricks)
    /**
     * Returns the number of tricks won by a specific player, defaulting to 0 if not found.
     *
     * @param p the player ID.
     * @return the number of tricks won.
     */
    def apply(p: PlayerId): Trick = t.getOrElse(p, 0)

    /**
     * Increments the trick count by 1 for the player who won the current trick.
     *
     * @param p the player ID of the trick winner.
     * @return the updated [[Tricks]] collection.
     */
    def addTrickTo(p: PlayerId): Tricks = t.updated(p, t.getOrElse(p, 0) + 1)
