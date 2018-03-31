package com.ll.domain.games.persistence

import com.ll.domain.auth.UserId
import com.ll.domain.games.GameId
import com.ll.domain.games.riichi.Tile

sealed trait OutEvent

sealed trait GeneralEvent extends OutEvent
sealed trait PlayerEvent extends OutEvent {
  def userId: UserId
}

object GeneralEvent {
  case class PlayerJoinedGame(id: UserId) extends GeneralEvent
}

object PlayerEvent {
  case class RiichiGameState(
    userId: UserId,
    gameId: GameId,
    closedHand: List[Tile],
    openHand: List[Tile],
    currentTitle: Option[Tile],
    discard: List[Tile]
  ) extends PlayerEvent

  case class TileFromWall(
    userId: UserId,
    tile: Tile
  ) extends PlayerEvent

  case class TileDiscarded(
    userId: UserId,
    tile: Tile
  ) extends PlayerEvent

  case class ValidationError(
    userId: UserId,
    error: ValidationError
  ) extends PlayerEvent
}