package it.unibo.pps.wizard.model.rules

import it.unibo.pps.wizard.model.basic.*

opaque type Trump = (Card, Option[Card.Color])
opaque type Round = Int
opaque type Bid = Int
opaque type BidsCollection = Map[PlayerId, Bid]
opaque type Scoreboard = Map[PlayerId, Int]
opaque type TricksWon = Map[PlayerId, Int]


//EVERY OPAQUE TYPE SHOULD BE IMPLEMENTED INTO HIS SRP file
//  - like Player, Table