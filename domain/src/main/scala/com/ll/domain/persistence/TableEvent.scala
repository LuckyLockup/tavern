package com.ll.domain.persistence

import com.ll.domain.auth.UserId
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.HumanPlayer
import com.ll.domain.games.deck.Tile
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.riichi.RiichiConfig
import com.ll.domain.games.riichi.result.GameScore
import com.ll.domain.games.{GameId, GameType, TableId}
import com.ll.domain.ws.WsMsgIn.RiichiGameCmd.RiichiCmd

sealed trait TableEvent[GT <: GameType] {def tableId: TableId}
sealed trait GameEvent[GT <: GameType] extends TableEvent[GT] {
  def tableId: TableId
  def gameId: GameId
  def position: PlayerPosition[GT]
  def turn: Int
}

object RiichiEvent {
  case class GameStarted(tableId: TableId, gameId: GameId, config: RiichiConfig) extends TableEvent[Riichi]
  case class GamePaused(tableId: TableId, gameId: GameId, turn: Int) extends TableEvent[Riichi]
  case class PlayerJoined(tableId: TableId, player: HumanPlayer[Riichi]) extends TableEvent[Riichi]
  case class PlayerLeft(tableId: TableId, player: HumanPlayer[Riichi]) extends TableEvent[Riichi]
  case class GameScored(
    tableId: TableId,
    gameId: GameId,
    turn: Int,
    score: GameScore
  ) extends TableEvent[Riichi]


  sealed trait RiichiGameEvent extends GameEvent[Riichi]
  case class TileDiscared(
    tableId: TableId,
    gameId: GameId,
    tile: Tile,
    turn: Int,
    position: PlayerPosition[Riichi],
    commands: Map[PlayerPosition[Riichi], List[RiichiCmd]]
  ) extends RiichiGameEvent

  case class TileFromTheWallTaken(
    tableId: TableId,
    gameId: GameId,
    tile: Tile,
    turn: Int,
    position: PlayerPosition[Riichi],
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
    position: PlayerPosition[Riichi]
  ) extends RiichiGameEvent

  case class TileClaimed(
    tableId: TableId,
    gameId: GameId,
    tile: Tile,
    turn: Int,
    position: PlayerPosition[Riichi]
  ) extends RiichiGameEvent

  case class GameFinished(
    tableId: TableId,
    gameId: GameId,
    winner: UserId,
    turn: Int,
    position: PlayerPosition[Riichi]
  ) extends RiichiGameEvent
}

