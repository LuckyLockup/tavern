package com.ll.domain.persistence

import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.riichi.RiichiConfig
import com.ll.domain.games.{GameId, GameType, TableId}
import com.ll.domain.messages.WsMsg

sealed trait TableCmd extends WsMsg.In {def tableId: TableId}
sealed trait UserCmd extends TableCmd {
  def userId: UserId
}

sealed trait GameCmd[GT <: GameType] extends TableCmd {
  def gameId: GameId
  def position: Option[Either[UserId, PlayerPosition[GT]]]
  def updatePosition(position: Either[UserId, PlayerPosition[GT]]): GameCmd[GT]
}

object UserCmd {
  case class GetState(tableId: TableId, userId: UserId) extends UserCmd

  case class JoinAsSpectacular(tableId: TableId, user: User) extends UserCmd {
    def userId = user.id
  }
  case class LeftAsSpectacular(tableId: TableId, user: User) extends UserCmd {
    def userId = user.id
  }

  case class JoinAsPlayer(tableId: TableId, user: User) extends UserCmd {
    def userId = user.id
  }
  case class LeftAsPlayer(tableId: TableId, user: User) extends UserCmd  {
    def userId = user.id
  }
}


object RiichiGameCmd {
  case class StartGame(tableId: TableId, gameId: GameId, config: RiichiConfig) extends GameCmd[Riichi] {
    def position = None
    def updatePosition(position: Either[UserId, PlayerPosition[Riichi]]): StartGame = this
  }
  case class PauseGame(tableId: TableId, gameId: GameId) extends GameCmd[Riichi] {
    def position = None
    def updatePosition(position: Either[UserId, PlayerPosition[Riichi]]): PauseGame = this
  }

  case class DiscardTile(
    tableId: TableId,
    gameId: GameId,
    tile: String,
    turn: Int,
    position: Option[Either[UserId, PlayerPosition[Riichi]]] = None) extends GameCmd[Riichi] {
    def updatePosition(position: Either[UserId, PlayerPosition[Riichi]]): DiscardTile =
      this.copy(position = Some(position))
  }

  case class GetTileFromWall(
    tableId: TableId,
    userId: UserId,
    gameId: GameId,
    position: Option[Either[UserId, PlayerPosition[Riichi]]] = None
   ) extends GameCmd[Riichi] {
    def updatePosition(position: Either[UserId, PlayerPosition[Riichi]]): GetTileFromWall =
      this.copy(position = Some(position))
  }

  case class ClaimTile(
    tableId: TableId,
    userId: UserId,
    gameId: GameId,
    tile: String,
    position: Option[Either[UserId, PlayerPosition[Riichi]]] = None
  ) extends GameCmd[Riichi]{
    def updatePosition(position: Either[UserId, PlayerPosition[Riichi]]): ClaimTile =
      this.copy(position = Some(position))
  }


  case class DeclareRon(
    tableId: TableId,
    userId: UserId,
    gameId: GameId,
    position: Option[Either[UserId, PlayerPosition[Riichi]]] = None
  ) extends GameCmd[Riichi]{
    def updatePosition(position: Either[UserId, PlayerPosition[Riichi]]): DeclareRon =
      this.copy(position = Some(position))
  }
}

