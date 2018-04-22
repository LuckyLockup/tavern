package com.ll.domain.messages

import com.ll.domain.Const
import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.persistence.{RiichiEvent, TableEvent}
import com.ll.domain.ws.WsMsgIn.RiichiGameCmd.RiichiCmd
import com.ll.domain.ws.WsMsgOut

object WsMsgProjector {
  implicit class TableProjector[GT <: GameType](event: TableEvent[GT]){
    def projection(position: Option[PlayerPosition[GT]] = None) = {
      (event, position) match {
        case (ev: TableEvent[Riichi], p: Option[PlayerPosition[Riichi]]) => riichiProjection(ev, p)
      }
    }
  }

  def riichiProjection(event: TableEvent[Riichi], position: Option[PlayerPosition[Riichi]] = None): WsMsgOut = {
    event match {
      case RiichiEvent.GameStarted(tableId, gameId, config) =>
        WsMsgOut.Riichi.GameStarted(tableId, gameId, 1)
      case RiichiEvent.GamePaused(tableId, gameId, turn)          =>
        WsMsgOut.Riichi.GamePaused(tableId, gameId, turn)
      case RiichiEvent.PlayerJoined(tableId, player)        =>
        WsMsgOut.Riichi.PlayerJoinedTable(tableId, player)
      case RiichiEvent.PlayerLeft(tableId, player)          =>
        WsMsgOut.Riichi.PlayerLeftTable(tableId, player)
      case RiichiEvent.TileDiscared(tableId, gameId, tile, turn, pos, cmds) =>
        val cmdsToPlayer: List[RiichiCmd] = position.flatMap(p => cmds.get(p)).toList.flatten
        WsMsgOut.Riichi.TileDiscarded(tableId, gameId, tile.repr, turn, pos, cmdsToPlayer)
      case RiichiEvent.TileFromTheWallTaken(tableId, gameId, tile, turn, playerPosition) =>
        val tileRepr = position.filter(p => p == playerPosition).map(_ => tile.repr).getOrElse(Const.ClosedTile)
        WsMsgOut.Riichi.TileFromWallTaken(tableId, gameId, tileRepr, turn, playerPosition)
    }
  }
}
