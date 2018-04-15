package com.ll.domain.ai

import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi

sealed trait AIType[GT <: GameType]

object AIType {
  /**
    * This is basic AI variant which can only discard whatever title it gets from the wall.
    */
  object Riichi {
    case object Duck extends AIType[Riichi]
  }
}