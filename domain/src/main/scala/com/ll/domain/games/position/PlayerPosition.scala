package com.ll.domain.games.position

import com.ll.domain.games.GameType

sealed trait PlayerPosition[GT <: GameType] {
  def nextPosition: PlayerPosition[GT]
}

object PlayerPosition {
  object RiichiPosition {
    case object EastPosition extends PlayerPosition[GameType.Riichi] {def nextPosition = SouthPosition}
    case object SouthPosition extends PlayerPosition[GameType.Riichi] {def nextPosition = WestPosition}
    case object WestPosition extends PlayerPosition[GameType.Riichi] {def nextPosition = NorthPosition}
    case object NorthPosition extends PlayerPosition[GameType.Riichi] {def nextPosition = EastPosition}
  }
}
