package it.unibo.pps.wizard.model.rules

import it.unibo.pps.wizard.model.basic.*

opaque type Round = Int
opaque type Bid = Int
opaque type BidsCollection = Map[PlayerId, Bid]
opaque type TricksWon = BidsCollection
opaque type Scoreboard = Map[PlayerId, Int]


//EVERY OPAQUE TYPE SHOULD BE IMPLEMENTED INTO HIS SRP file
//  - like Player, Table