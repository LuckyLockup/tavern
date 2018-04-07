package com.ll.domain.messages

import com.ll.domain.auth.UserId
import com.ll.domain.games.{GameId, TableId}


object WsMsg {
  trait In
  trait Out

  object In {
    case class Ping(id: Int) extends In
  }

  object Out {
    sealed trait Table extends Out

    case class Pong(id: Int) extends Out
    case class Text(txt: String) extends Out

    case class SpectacularJoinedTable(userId: UserId, tableId: TableId) extends Out
    case class SpectacularLeftTable(userId: UserId, tableId: TableId) extends Out

    object Table {
      case class TableState(tableId: TableId) extends Table
      case class GameStarted(tableId: TableId, gameId: GameId) extends Table
      case class GamePaused(tableId: TableId, gameId: GameId) extends Table
    }
  }
}