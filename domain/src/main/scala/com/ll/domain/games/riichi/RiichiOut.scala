package com.ll.domain.games.riichi

import com.ll.domain.games.TableId
import com.ll.domain.messages.WsMsg

sealed trait RiichiOut extends WsMsg.Out.GameEvent

object RiichiOut {
  case class TileFromWallTaken(
    tableId: TableId,
    position: RiichiPosition,
    tile: Option[String]
  ) extends RiichiOut

  case class TileDiscarded(
    tableId: TableId,
    position: RiichiPosition,
    tile: String
  ) extends RiichiOut
}
