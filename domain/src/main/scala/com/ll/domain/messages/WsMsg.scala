package com.ll.domain.messages

import com.ll.domain.auth.User
import com.ll.domain.games.{GameId, TableId}


object WsMsg {
  trait In
  trait Out

  object In {
    case class Ping(id: Int) extends In
  }

  object Out {
    sealed trait Table extends Out { def tableId: TableId}

    case class Pong(id: Int) extends Out
    case class Text(txt: String) extends Out

    object Table {
      case class TableState(tableId: TableId) extends Table
      case class GameStarted(tableId: TableId, gameId: GameId) extends Table
      case class GamePaused(tableId: TableId, gameId: GameId) extends Table

      case class SpectacularJoinedTable(user: User, tableId: TableId) extends Out
      case class SpectacularLeftTable(user: User, tableId: TableId) extends Out
    }
  }
}
