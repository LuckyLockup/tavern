package com.ll.domain.persistence

import com.ll.domain.auth.UserId
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.Riichi.HumanPlayer
import com.ll.domain.games.deck.Tile
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.riichi.RiichiConfig
import com.ll.domain.games.{GameId, GameType, TableId}

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

  case class TileDiscared(
    tableId: TableId,
    gameId: GameId,
    tile: Tile,
    turn: Int,
    position: PlayerPosition[Riichi]
  ) extends GameEvent[Riichi]

  case class TileFromTheWall(
    tableId: TableId,
    gameId: GameId,
    tile: Tile,
    turn: Int,
    position: PlayerPosition[Riichi]
  ) extends GameEvent[Riichi]

  case class TileClaimed(
    tableId: TableId,
    gameId: GameId,
    tile: Tile,
    turn: Int,
    position: PlayerPosition[Riichi]
  ) extends GameEvent[Riichi]

  case class GameFinished(
    tableId: TableId,
    gameId: GameId,
    winner: UserId,
    turn: Int,
    position: PlayerPosition[Riichi]
  ) extends GameEvent[Riichi]
}

