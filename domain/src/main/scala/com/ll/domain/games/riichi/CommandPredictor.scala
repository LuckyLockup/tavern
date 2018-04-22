package com.ll.domain.games.riichi

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.deck.Tile
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.persistence.GameCmd

object CommandPredictor {

  def predictsCommands(discardedTile: Tile, table: GameStarted, discardedPosition: PlayerPosition[Riichi]):
   Map[PlayerPosition[Riichi], List[GameCmd[Riichi]]] = {
    Map.empty
  }

  def predictCommands(discardedTile: Tile, playerState: PlayerState): List[GameCmd[Riichi]] = {

    ???
  }
}
