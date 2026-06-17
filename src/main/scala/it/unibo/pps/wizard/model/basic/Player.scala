package it.unibo.pps.wizard.model.basic

/**
 * Represent the hand of a player
 * */
final case class Hand(cards: Set[Card]):
  def remove(card: Card): Option[Hand] =
    if cards.contains(card) then Some(copy(cards - card))
    else None
  def size: Int = cards.size

/**
 * Represent a player in the game that can be human or computer.
 * Each player has an id, a name, a hand of cards, a bid (number of tricks they think they will win),
 * the number of tricks they have won so far and their current points.
 * */
type PlayerId = Int
final case class Player(
    id: PlayerId,
    name: String,
    hand: Hand = Hand(Set.empty),
    bid: Option[Int] = None,
    tricksWon: Int = 0,
    points: Int = 0
):
  def receiveCards(newCards: List[Card]): Player =
    this.copy(hand = Hand(newCards.toSet))
    
  def playCard(card: Card): Either[String, (Player, Card)] =
    hand.remove(card) match
      case Some(updatedHand) => Right((this.copy(hand = updatedHand), card))
      case None => Left(s"Card $card not in player's hand")

object Player:
  def human(id: PlayerId, name: String): Player = Player(id, name)
  def computer(id: PlayerId): Player = Player(id, s"Computer $id")