package com.ll.domain.messages

import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.messages.WsMsg.Out.Table
import com.ll.domain.persistence.{TableEvent, TableState}

object WsMsgProjector {

  def convert[E <: TableEvent](event: E, state: TableState[_, E]): Out = event match {
    case TableEvent.GameStarted(tableId, gameId) =>
      Table.GameStarted(tableId, gameId)
    case TableEvent.GamePaused(tableId, gameId) =>
      Table.GamePaused(tableId, gameId)
  }
}
