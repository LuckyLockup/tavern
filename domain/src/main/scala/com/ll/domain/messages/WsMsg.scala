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

    sealed trait GameEvent[GT<: GameType] extends Table {
      def turn: Int
    }
    sealed trait TableState[GT<: GameType] extends Table

    object Riichi {
      case class RiichiState(
        tableId: TableId,
        admin: User,
        states: List[RiichiPlayerState],
        uraDoras: List[String],
        deck: Int,
        turn: Int
      ) extends TableState[Riichi]

      case class RiichiPlayerState(
        player: Player[Riichi],
        closedHand: List[String],
        currentTile: Option[String] = None,
        discard: List[String] = Nil,
        online: Boolean = true
      )

      case class GameStarted(tableId: TableId, gameId: GameId, turn: Int) extends GameEvent[Riichi]
      case class GamePaused(tableId: TableId, gameId: GameId, turn: Int) extends GameEvent[Riichi]

      case class SpectacularJoinedTable(user: User, tableId: TableId) extends GameEvent[Riichi] {
        def turn = 0
      }
      case class SpectacularLeftTable(user: User, tableId: TableId) extends GameEvent[Riichi] {
        def turn = 0
      }
      case class PlayerJoinedTable(tableId: TableId, user: HumanPlayer[Riichi]) extends GameEvent[Riichi] {
        def turn = 0
      }
      case class PlayerLeftTable(tableId: TableId, user: HumanPlayer[Riichi]) extends GameEvent[Riichi] {
        def turn = 0
      }

      case class TileFromWallTaken(
        tableId: TableId,
        gameId: GameId,
        tile: String,
        turn: Int,
        position: PlayerPosition[Riichi]
      ) extends GameEvent[Riichi]

      case class TileDiscarded(
        tableId: TableId,
        gameId: GameId,
        tile: String,
        turn: Int,
        position: PlayerPosition[Riichi]
      ) extends GameEvent[Riichi]
    }
  }
}
