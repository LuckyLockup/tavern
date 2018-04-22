package com.ll.domain.games.riichi

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.deck.Tile
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.ws.WsMsgIn.GameCmd
import com.ll.domain.ws.WsMsgIn.RiichiGameCmd.RiichiCmd

object CommandPredictor {

  def predictsCommands(discardedTile: Tile, table: GameStarted, discardedPosition: PlayerPosition[Riichi]):
   Map[PlayerPosition[Riichi], List[RiichiCmd]] = {
    Map.empty
  }

  def predictCommands(discardedTile: Tile, playerState: PlayerState): List[GameCmd[Riichi]] = {

    ???
  }
}
