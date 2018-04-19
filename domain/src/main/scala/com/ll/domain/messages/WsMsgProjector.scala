package com.ll.domain.messages

import com.ll.domain.games.GameType
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.messages.WsMsg.Out.Riichi
import com.ll.domain.persistence.{RiichiEvent, TableEvent, TableState}

object WsMsgProjector {

  //TODO change to implicit class
  def projection[GT<: GameType](event: TableEvent[GT], position: Option[PlayerPosition[GT]]): Out = event match {
    case RiichiEvent.GameStarted(tableId, gameId, config)  =>
      Riichi.GameStarted(tableId, gameId)
    case RiichiEvent.GamePaused(tableId, gameId)  =>
      Riichi.GamePaused(tableId, gameId)
    case RiichiEvent.PlayerJoined(tableId, player) =>
      Riichi.PlayerJoinedTable(tableId, player)
    case RiichiEvent.PlayerLeft(tableId, player)   =>
      Riichi.PlayerLeftTable(tableId, player)
  }
}
