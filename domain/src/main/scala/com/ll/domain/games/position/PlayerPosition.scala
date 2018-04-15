package com.ll.domain.games.position

import com.ll.domain.games.GameType

sealed trait PlayerPosition[T <: GameType]

object PlayerPosition {
  object RiichiPosition {
    case object EastPosition extends PlayerPosition[GameType.Riichi]
    case object SouthPosition extends PlayerPosition[GameType.Riichi]
    case object WestPosition extends PlayerPosition[GameType.Riichi]
    case object NorthPosition extends PlayerPosition[GameType.Riichi]
  }
}
