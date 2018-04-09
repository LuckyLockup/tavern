package com.ll.domain.persistence

import com.ll.domain.auth.UserId
import com.ll.domain.games.{GameId, TableId}
import com.ll.domain.messages.WsMsg

sealed trait TableCmd extends WsMsg.In {def tableId: TableId}
sealed trait UserCmd extends TableCmd {def userId: UserId}
sealed trait RiichiCmd extends TableCmd {def gameId: GameId}

object TableCmd {
  case class StartGame(tableId: TableId) extends TableCmd
  case class PauseGame(tableId: TableId) extends TableCmd
}

object UserCmd {
  case class GetState(tableId: TableId, userId: UserId) extends UserCmd
  case class JoinAsPlayer(tableId: TableId, userId: UserId) extends UserCmd
  case class LeftAsPlayer(tableId: TableId, userId: UserId) extends UserCmd
  case class JoinAsSpectacular(tableId: TableId, userId: UserId) extends UserCmd
  case class LeftAsSpectacular(tableId: TableId, userId: UserId) extends UserCmd
}

object RiichiCmd {
  case class DiscardTile(
    tableId: TableId,
    userId: UserId,
    gameId: GameId,
    tile: String) extends RiichiCmd

  case class GetTileFromWall(
    tableId: TableId,
    userId: UserId,
    gameId: GameId) extends RiichiCmd

  case class ClaimTile(
    tableId: TableId,
    userId: UserId,
    gameId: GameId,
    tile: String
  ) extends RiichiCmd

  case class DeclareWin(
    tableId: TableId,
    userId: UserId,
    gameId: GameId
  ) extends RiichiCmd
}

