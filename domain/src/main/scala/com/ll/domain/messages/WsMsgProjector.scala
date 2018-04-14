package com.ll.domain.messages

import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.messages.WsMsg.Out.Table
import com.ll.domain.persistence.{TableEvent, TableState, UserEvent}

object WsMsgProjector {

  def convert(event: TableEvent, state: TableState[_, _, _, _]): Out = event match {
    case TableEvent.GameStarted(tableId, gameId) =>
      Table.GameStarted(tableId, gameId)
    case TableEvent.GamePaused(tableId, gameId) =>
      Table.GamePaused(tableId, gameId)
    case UserEvent.PlayerJoined(tableId, player) =>
      Table.PlayerJoinedTable(tableId, player)
    case UserEvent.PlayerLeft(tableId, player) =>
      Table.PlayerLeftTable(tableId, player)
  }
}
