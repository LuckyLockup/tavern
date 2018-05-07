package com.ll.domain.games.deck

import com.ll.domain.games.GameType
import com.ll.domain.games.position.PlayerPosition

case class DiscardedTile[GT<: GameType](tile: Tile, turn: Int, position: PlayerPosition[GT])
