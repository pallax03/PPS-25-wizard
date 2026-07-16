package it.unibo.pps.wizard.application.scalafx.util

import it.unibo.pps.wizard.engine.model.basic.cards.{Card => WizardCard}
import scalafx.scene.paint.Color

object WizardTheme:
  object Colors:
    val white: Color = Color.White
    val textMuted: Color = Color.rgb(178, 190, 195)
    val textSoft: Color = Color.White
    val roleBot: Color = Color.rgb(140, 140, 140)
    val roleHuman: Color = Color.web("#D95D39")
    val following: String = "#ff6b6b"
    val winning: String = "#00D4FF"
    val warning: String = "#FFCC00"

  object Player:
    private val cornerRadii = "-fx-background-radius: 10; -fx-border-radius: 10;"
    private val rowCornerRadii = "-fx-background-radius: 0 0 8 8;"
    private val border = "#636e72"
    private val biddingAccent = "#D95D39"
    private val playingAccent = "#007C89"

    val normalStyle: String =
      s"-fx-background-color: rgba(45, 52, 54, 0.7); $cornerRadii -fx-border-color: $border; -fx-border-width: 2;"

    val rowBottomRadius: String = rowCornerRadii
    val cellSeparatorStyle: String = s"-fx-border-color: $border; -fx-border-width: 0 1 0 0;"
    val rowBorderStyle: String = s"-fx-border-color: $border; -fx-border-width: 0 0 1 0;"
    val activeBidRowStyle: String =
      s"-fx-background-color: rgba(217, 93, 57, 0.35); $rowCornerRadii"
    val errorBidRowStyle: String =
      s"-fx-background-color: rgba(255, 0, 0, 0.4); $rowCornerRadii"
    val errorBidFieldStyle: String =
      "-fx-border-color: red; -fx-border-width: 2; -fx-border-radius: 3;"

    def activeStyle(phase: UiPhase): String =
      val (background, borderColor) =
        if phase.isPlaying then ("rgba(0, 124, 137, 0.30)", playingAccent)
        else ("rgba(217, 93, 57, 0.30)", biddingAccent)
      s"-fx-background-color: $background; $cornerRadii -fx-border-color: $borderColor; -fx-border-width: 2;"

  object GameInfo:
    def colorFor(phase: UiPhase): Color = phase match
      case UiPhase.Bidding | UiPhase.ChoosingTrump => Color.web("#D95D39")
      case UiPhase.Playing                         => Color.web("#00A3AD")
      case _                                       => Colors.white

  object Table:
    val normalStyle: String =
      "-fx-background-color: rgba(43, 92, 63, 0.85); -fx-background-radius: 15;"
    val hoverStyle: String =
      "-fx-background-color: rgba(60, 120, 80, 0.95); -fx-background-radius: 15;"
    val cardWrapperStyle: String =
      "-fx-padding: 8; -fx-background-color: transparent; -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: transparent; -fx-border-width: 2;"
    val winningCardWrapperStyle: String =
      s"-fx-padding: 8; -fx-background-color: rgba(0, 212, 255, 0.10); -fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: ${Colors.winning}; -fx-border-width: 2;"

  object Card:
    def colorFor(color: WizardCard.Color): Color = color match
      case WizardCard.Color.Blue   => Color.DodgerBlue
      case WizardCard.Color.Green  => Color.ForestGreen
      case WizardCard.Color.Red    => Color.FireBrick
      case WizardCard.Color.Yellow => Color.Goldenrod
