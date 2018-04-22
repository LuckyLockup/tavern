package com.ll.domain.messages

import com.ll.domain.Const
import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.messages.WsMsg.Out.Riichi
import com.ll.domain.persistence.{GameCmd, RiichiEvent, TableEvent, TableState}

import scala.collection.immutable


object WsMsgProjector {
  implicit class TableProjector[GT <: GameType](event: TableEvent[GT]){
    def projection(position: Option[PlayerPosition[GT]] = None) = {
      (event, position) match {
        case (ev: TableEvent[Riichi], p: Option[PlayerPosition[Riichi]]) => riichiProjection(ev, p)
      }
    }
  }

  def riichiProjection(event: TableEvent[Riichi], position: Option[PlayerPosition[Riichi]] = None): Out = {
    event match {
      case RiichiEvent.GameStarted(tableId, gameId, config) =>
        Riichi.GameStarted(tableId, gameId, 1)
      case RiichiEvent.GamePaused(tableId, gameId, turn)          =>
        Riichi.GamePaused(tableId, gameId, turn)
      case RiichiEvent.PlayerJoined(tableId, player)        =>
        Riichi.PlayerJoinedTable(tableId, player)
      case RiichiEvent.PlayerLeft(tableId, player)          =>
        Riichi.PlayerLeftTable(tableId, player)
      case RiichiEvent.TileDiscared(tableId, gameId, tile, turn, pos, cmds) =>
        //TODO send real cmds
        val cmdsToPlayer: List[GameCmd[Riichi]] = position.flatMap(p => cmds.get(p)).toList.flatten
        Riichi.TileDiscarded(tableId, gameId, tile.repr, turn, pos, Nil)
      case RiichiEvent.TileFromTheWallTaken(tableId, gameId, tile, turn, playerPosition) =>
        val tileRepr = position.filter(p => p == playerPosition).map(_ => tile.repr).getOrElse(Const.ClosedTile)
        Riichi.TileFromWallTaken(tableId, gameId, tileRepr, turn, playerPosition)
    }
  }
}
