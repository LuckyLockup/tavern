package com.ll.domain.ws

import com.ll.domain.Const
import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.persistence.{RiichiEvent, TableEvent}

object WsMsgOutProjector {
  implicit class TableProjector[GT <: GameType](event: TableEvent[GT]){
    def projection(position: Option[PlayerPosition[GT]]): WsMsgOut = {
      (event, position) match {
        case (ev: TableEvent[Riichi], p: Option[PlayerPosition[Riichi]]) => riichiProjection(ev, p)
      }
    }
  }

  private def riichiProjection(event: TableEvent[Riichi], position: Option[PlayerPosition[Riichi]] = None): WsMsgOut = {
    event match {
      case RiichiEvent.PlayerJoined(tableId, player) =>
        WsMsgOut.Riichi.PlayerJoinedTable(tableId, player)
//      case RiichiEvent.PlayerLeft(tableId, player) =>
//        WsMsgOut.Riichi.PlayerLeft(tableId, player)
      case RiichiEvent.GameStarted(tableId, gameId, config) =>
        WsMsgOut.Riichi.GameStarted(tableId, gameId, 1)
      case RiichiEvent.GamePaused(tableId, gameId, turn)          =>
        WsMsgOut.Riichi.GamePaused(tableId, gameId, turn)
      case RiichiEvent.TileDiscared(tableId, gameId, tile, turn, pos, cmds) =>
        val cmdsToPlayer = position.flatMap(p => cmds.get(p)).toList.flatten
        WsMsgOut.Riichi.TileDiscarded(tableId, gameId, tile.repr, turn, pos, cmdsToPlayer)
      case RiichiEvent.TileFromTheWallTaken(tableId, gameId, tile, turn, playerPosition, cmds) =>
        if (position.contains(playerPosition)) {
          WsMsgOut.Riichi.TileFromWallTaken(tableId, gameId, tile.repr, turn, playerPosition, cmds)
        } else {
          WsMsgOut.Riichi.TileFromWallTaken(tableId, gameId, Const.ClosedTile, turn, playerPosition, Nil)
        }
      case RiichiEvent.TsumoDeclared(tableId, gameId, turn, pos) =>
        WsMsgOut.Riichi.TsumoDeclared(tableId, gameId, turn, pos)
      case RiichiEvent.GameScored(tableId, gameId, turn, gameScore) =>
        WsMsgOut.Riichi.GameScored(tableId, gameId, turn, gameScore)
      case RiichiEvent.TileClaimed(tableId, gameId, set, pos) =>
        WsMsgOut.Riichi.TileClaimed(
          tableId = tableId,
          gameId = gameId,
          set = set.set.repr,
          tiles = set.set.tiles,
          from = set.from,
          turn = set.turn,
          position = pos
        )
    }
  }
}
