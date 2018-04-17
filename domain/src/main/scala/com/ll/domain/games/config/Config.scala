package com.ll.domain.games.config

import com.ll.domain.ai.AIType
import com.ll.domain.games.GameType

sealed trait Config[GT<: GameType]


case class Riichi(
  defaultEastAi: AIType[GameType.Riichi] = AIType.Riichi.Duck,
  defaultSouthAi: AIType[GameType.Riichi] = AIType.Riichi.Duck,
  defaultWestAi: AIType[GameType.Riichi] = AIType.Riichi.Duck,
  defaultNorthAi: AIType[GameType.Riichi] = AIType.Riichi.Duck
) extends Config[GameType.Riichi]

