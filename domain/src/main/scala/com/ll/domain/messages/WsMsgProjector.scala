package com.ll.domain.messages

import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.messages.WsMsg.Out.Riichi
import com.ll.domain.persistence.{RiichiEvent, TableEvent, TableState}


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
        Riichi.GameStarted(tableId, gameId)
      case RiichiEvent.GamePaused(tableId, gameId)          =>
        Riichi.GamePaused(tableId, gameId)
      case RiichiEvent.PlayerJoined(tableId, player)        =>
        Riichi.PlayerJoinedTable(tableId, player)
      case RiichiEvent.PlayerLeft(tableId, player)          =>
        Riichi.PlayerLeftTable(tableId, player)
    }
  }
}
