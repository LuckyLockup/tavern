package com.ll.domain.persistence

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.deck.Tile
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.riichi.RiichiConfig
import com.ll.domain.games.riichi.result.GameScore
import com.ll.domain.games.{GameId, GameType, Player, TableId}
import com.ll.domain.persistence.TableCmd.RiichiCmd
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd

sealed trait TableEvent[GT <: GameType] {def tableId: TableId}
sealed trait GameEvent[GT <: GameType] extends TableEvent[GT] {
  def gameId: GameId
  def turn: Int
  def position: PlayerPosition[GT]
}

object RiichiEvent {
  case class PlayerJoined(tableId: TableId, player: Player[Riichi]) extends TableEvent[Riichi]

  case class PlayerLeft(tableId: TableId, player: Player[Riichi]) extends TableEvent[Riichi]

  case class GameStarted(tableId: TableId, gameId: GameId, config: RiichiConfig) extends TableEvent[Riichi]

  case class GamePaused(tableId: TableId, gameId: GameId, turn: Int) extends TableEvent[Riichi]

  case class DoubleRonDeclared(
    tableId: TableId,
    gameId: GameId,
    turn: Int,
    from: PlayerPosition[Riichi],
    positions: (PlayerPosition[Riichi], PlayerPosition[Riichi])
  ) extends TableEvent[Riichi]

  case class DrawDeclared(
    tableId: TableId,
    gameId: GameId,
    turn: Int
  ) extends TableEvent[Riichi]

  sealed trait RiichiGameEvent extends GameEvent[Riichi]

  case class ActionSkipped(
    tableId: TableId,
    gameId: GameId,
    turn: Int,
    position: PlayerPosition[Riichi]
  ) extends RiichiGameEvent

  case class PendingEvent(
    event: RiichiGameEvent
  ) extends RiichiGameEvent {
    def tableId = event.tableId
    def gameId = event.gameId
    def turn = event.turn
    def position = event.position
  }

  case class TileDiscared(
    tableId: TableId,
    gameId: GameId,
    turn: Int,
    position: PlayerPosition[Riichi],
    tile: Tile,
    commands: List[RiichiCmd]
  ) extends RiichiGameEvent

  case class TileFromTheWallTaken(
    tableId: TableId,
    gameId: GameId,
    turn: Int,
    position: PlayerPosition[Riichi],
    tile: String,
    commands: List[RiichiCmd]
  ) extends RiichiGameEvent

  case class TsumoDeclared(
    tableId: TableId,
    gameId: GameId,
    turn: Int,
    position: PlayerPosition[Riichi]
  ) extends RiichiGameEvent

  case class RonDeclared(
    tableId: TableId,
    gameId: GameId,
    turn: Int,
    position: PlayerPosition[Riichi],
    from: PlayerPosition[Riichi],
  ) extends RiichiGameEvent

  case class PungClaimed(
    tableId: TableId,
    gameId: GameId,
    turn: Int,
    position: PlayerPosition[Riichi],
    tiles: List[String],
    from: PlayerPosition[Riichi]
  ) extends RiichiGameEvent

  case class ChowClaimed(
    tableId: TableId,
    gameId: GameId,
    turn: Int,
    position: PlayerPosition[Riichi],
    tiles: List[String],
    from: PlayerPosition[Riichi]
  ) extends RiichiGameEvent
}

