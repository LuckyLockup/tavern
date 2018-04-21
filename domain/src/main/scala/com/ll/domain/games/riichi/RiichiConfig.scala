package com.ll.domain.games.riichi

import com.ll.domain.ai.AIType
import com.ll.domain.games.GameType.Riichi
import scala.concurrent.duration._

case class RiichiConfig(
  defaultEastAi: AIType[Riichi] = AIType.Riichi.Duck,
  defaultSouthAi: AIType[Riichi] = AIType.Riichi.Duck,
  defaultWestAi: AIType[Riichi] = AIType.Riichi.Duck,
  defaultNorthAi: AIType[Riichi] = AIType.Riichi.Duck,
  nextTileDelay: FiniteDuration = 1.seconds
)
