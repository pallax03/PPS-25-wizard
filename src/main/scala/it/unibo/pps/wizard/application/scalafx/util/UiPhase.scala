package it.unibo.pps.wizard.application.scalafx.util

enum UiPhase:
  case ChoosingTrump, Bidding, Playing, Ended, Unknown

  def isBidding: Boolean = this == UiPhase.Bidding
  def isPlaying: Boolean = this == UiPhase.Playing

  def label: String = this match
    case UiPhase.ChoosingTrump => "Choosing Trump"
    case UiPhase.Bidding       => "Bidding"
    case UiPhase.Playing       => "Playing"
    case UiPhase.Ended         => "Ended"
    case UiPhase.Unknown       => ""

object UiPhase:
  def fromName(name: String): UiPhase = name match
    case "ChoosingTrump" => ChoosingTrump
    case "Bidding"       => Bidding
    case "Playing"       => Playing
    case "Ended"         => Ended
    case _               => Unknown
