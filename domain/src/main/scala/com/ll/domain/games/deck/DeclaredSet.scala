package com.ll.domain.games.deck

import com.ll.domain.games.GameType
import com.ll.domain.games.position.PlayerPosition

case class DeclaredSet[GT<: GameType](set: TileSet, position: PlayerPosition[GT])
