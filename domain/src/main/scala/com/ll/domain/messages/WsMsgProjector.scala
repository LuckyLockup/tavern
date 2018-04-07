package com.ll.domain.messages

import com.ll.domain.games.GameTable
import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.messages.WsMsg.Out.Table
import com.ll.domain.persistence.TableEvent

object WsMsgProjector {

  def convert(event: TableEvent, state: GameTable[_, _, _]): Out = event match {
    case TableEvent.GameStarted(tableId, gameId) =>
      Table.GameStarted(tableId, gameId)
    case TableEvent.GamePaused(tableId, gameId) =>
      Table.GamePaused(tableId, gameId)
  }
}
