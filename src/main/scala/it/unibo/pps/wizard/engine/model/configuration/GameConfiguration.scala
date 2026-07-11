package it.unibo.pps.wizard.engine.model.configuration

enum BotsDifficulty:
  case Dumb
  case Prolog

case class GameConfiguration(
    playerName: String,
    numberOfBots: Int,
    botsDifficulty: BotsDifficulty
)
