package com.ll.domain.persistence

import com.ll.domain.auth.UserId
import com.ll.domain.games.Player.HumanPlayer
import com.ll.domain.games.deck.Tile
import com.ll.domain.games.riichi.RiichiPosition
import com.ll.domain.games.{GameId, PlayerPosition, TableId}

sealed trait TableEvent {def tableId: TableId}
sealed trait UserEvent extends TableEvent {def userId: UserId}
sealed trait GameEvent[P <: PlayerPosition] extends TableEvent {
  def gameId: GameId
  def position: P
  def turn: Int
}
sealed trait RiichiEvent extends GameEvent[RiichiPosition] {
  def position: RiichiPosition
}

object TableEvent {
  case class GameStarted(tableId: TableId, gameId: GameId) extends TableEvent
  case class GamePaused(tableId: TableId, gameId: GameId) extends TableEvent
}

object UserEvent {
  case class PlayerJoined(tableId: TableId, player: HumanPlayer) extends UserEvent {
    def userId = player.user.id
  }
  case class PlayerLeft(tableId: TableId, player: HumanPlayer) extends UserEvent {
    def userId = player.user.id
  }
}

object RiichiEvent {
  case class TileDiscared(
    tableId: TableId,
    userId: UserId,
    gameId: GameId,
    tile: Tile,
    turn: Int,
    position: RiichiPosition
  ) extends RiichiEvent

  case class TileFromTheWall(
    tableId: TableId,
    userId: UserId,
    gameId: GameId,
    tile: Tile,
    turn: Int,
    position: RiichiPosition
  ) extends RiichiEvent

  case class TileClaimed(
    tableId: TableId,
    userId: UserId,
    gameId: GameId,
    tile: Tile,
    turn: Int,
    position: RiichiPosition
  ) extends RiichiEvent

  case class GameFinished(
    tableId: TableId,
    gameId: GameId,
    winner: UserId,
    turn: Int,
    position: RiichiPosition
  ) extends RiichiEvent
}

