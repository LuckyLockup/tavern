package com.ll.domain.persistence

import com.ll.domain.auth.UserId
import com.ll.domain.games.Player.HumanPlayer
import com.ll.domain.games.deck.Tile
import com.ll.domain.games.{GameId, TableId}

sealed trait TableEvent {def tableId: TableId}
sealed trait UserEvent extends TableEvent {def userId: UserId}
sealed trait GameEvent extends TableEvent {def gameId: GameId}
sealed trait RiichiEvent extends GameEvent {def turn: Int}

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
    turn: Int
  ) extends RiichiEvent

  case class TileFromTheWall(
    tableId: TableId,
    userId: UserId,
    gameId: GameId,
    tile: Tile,
    turn: Int
  ) extends RiichiEvent

  case class TileClaimed(
    tableId: TableId,
    userId: UserId,
    gameId: GameId,
    tile: Tile,
    turn: Int
  ) extends RiichiEvent

  case class GameFinished(
    tableId: TableId,
    gameId: GameId,
    winner: UserId,
    turn: Int
  ) extends RiichiEvent
}

