package com.ll.domain.persistence

import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.{GameId, HumanPlayer, TableId}
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
  case class JoinAsPlayer(tableId: TableId, player: HumanPlayer) extends UserCmd {
    def userId = player.userId
  }
  case class LeftAsPlayer(tableId: TableId, player: HumanPlayer) extends UserCmd  {
    def userId = player.userId
  }
  case class JoinAsSpectacular(tableId: TableId, user: User) extends UserCmd {
    def userId = user.id
  }
  case class LeftAsSpectacular(tableId: TableId, user: User) extends UserCmd {
    def userId = user.id
  }
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

