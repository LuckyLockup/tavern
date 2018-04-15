package com.ll.domain.messages

import com.ll.domain.auth.User
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.Riichi.HumanPlayer
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.{GameId, GameType, Player, TableId}


object WsMsg {
  trait In
  trait Out

  object In {
    case class Ping(id: Int) extends In
  }

  object Out {
    sealed trait Table extends Out { def tableId: TableId}
    case class Pong(id: Int) extends Out
    case class Message(txt: String) extends Out
    case class ValidationError(reason: String) extends Out

    sealed trait GameEvent[GT<: GameType] extends Table
    sealed trait TableState[GT<: GameType] extends Table

    object Riichi {
      case class RiichiState(
        tableId: TableId,
        players: List[Player[Riichi]]
      ) extends TableState[Riichi]

      case class GameStarted(tableId: TableId, gameId: GameId) extends GameEvent[Riichi]
      case class GamePaused(tableId: TableId, gameId: GameId) extends GameEvent[Riichi]

      case class SpectacularJoinedTable(user: User, tableId: TableId) extends GameEvent[Riichi]
      case class SpectacularLeftTable(user: User, tableId: TableId) extends GameEvent[Riichi]
      case class PlayerJoinedTable(tableId: TableId, user: HumanPlayer) extends GameEvent[Riichi]
      case class PlayerLeftTable(tableId: TableId, user: HumanPlayer) extends GameEvent[Riichi]

      case class TileFromWallTaken(
        tableId: TableId,
        position: PlayerPosition[Riichi],
        tile: Option[String]
      ) extends GameEvent[Riichi]

      case class TileDiscarded(
        tableId: TableId,
        position: PlayerPosition[Riichi],
        tile: String
      ) extends GameEvent[Riichi]
    }
  }
}
