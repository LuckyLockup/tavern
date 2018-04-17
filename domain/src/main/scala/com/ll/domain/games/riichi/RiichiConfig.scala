package com.ll.domain.games.riichi

import com.ll.domain.ai.AIType
import com.ll.domain.games.GameType.Riichi

case class RiichiConfig(
  defaultEastAi: AIType[Riichi] = AIType.Riichi.Duck,
  defaultSouthAi: AIType[Riichi] = AIType.Riichi.Duck,
  defaultWestAi: AIType[Riichi] = AIType.Riichi.Duck,
  defaultNorthAi: AIType[Riichi] = AIType.Riichi.Duck
)
