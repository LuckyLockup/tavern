package com.ll.domain.games.persistence

import com.ll.domain.auth.UserId
import com.ll.domain.games.riichi.Tile

sealed trait Event

sealed trait GameEvent extends Event
sealed trait RiichiEvent extends Event

object GameEvent {
  case object GameStarted extends GameEvent
  case class PlayerJoinedTable(userId: UserId) extends GameEvent
}

object RiichiEvent {
  case class TileDiscarded(userId: UserId, tile: Tile) extends RiichiEvent
  case class TileTaken(userId: UserId) extends RiichiEvent
}
