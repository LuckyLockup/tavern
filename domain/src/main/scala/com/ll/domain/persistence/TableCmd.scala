package com.ll.domain.persistence

import com.ll.domain.ai.ServiceId
import com.ll.domain.auth.User
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.riichi.RiichiConfig
import com.ll.domain.games.{GameId, GameType, TableId}

sealed trait TableCmd[GT <: GameType] {
  def tableId: TableId
}

object TableCmd {
  case class JoinAsPlayer[GT <: GameType](tableId: TableId, user: Either[ServiceId, User]) extends TableCmd[GT]

  case class LeftAsPlayer[GT <: GameType](tableId: TableId, user: Either[ServiceId, User]) extends TableCmd[GT]

  case class GetState[GT <: GameType](tableId: TableId, position: Option[PlayerPosition[Riichi]]) extends TableCmd[GT]

  sealed trait RiichiCmd extends TableCmd[Riichi] {
    //TODO add position, turn, gameId
  }


  object RiichiCmd {
    case class StartGame(tableId: TableId, gameId: GameId, config: RiichiConfig) extends RiichiCmd

    case class PauseGame(tableId: TableId, gameId: GameId) extends RiichiCmd

    case class SkipAction(tableId: TableId, gameId: GameId, turn: Int, playerPosition: PlayerPosition[Riichi]) extends RiichiCmd

    case class DiscardTile(
      tableId: TableId,
      gameId: GameId,
      tile: String,
      turn: Int,
      position: PlayerPosition[Riichi]) extends RiichiCmd

    case class GetTileFromWall(
      tableId: TableId,
      gameId: GameId,
      turn: Int,
      position: PlayerPosition[Riichi]
    ) extends RiichiCmd

    case class ClaimPung(
      tableId: TableId,
      gameId: GameId,
      from: PlayerPosition[Riichi],
      turn: Int,
      tiles: List[String],
      position: PlayerPosition[Riichi]
    ) extends RiichiCmd

    case class ClaimChow(
      tableId: TableId,
      gameId: GameId,
      from: PlayerPosition[Riichi],
      turn: Int,
      tiles: List[String],
      position: PlayerPosition[Riichi]
    ) extends RiichiCmd

    case class DeclareRon(
      tableId: TableId,
      gameId: GameId,
      turn: Int,
      position: PlayerPosition[Riichi]
    ) extends RiichiCmd

    case class DeclareTsumo(
      tableId: TableId,
      gameId: GameId,
      position: PlayerPosition[Riichi]
    ) extends RiichiCmd

    case class ScoreGame(
      tableId: TableId,
      gameId: GameId
    ) extends RiichiCmd
  }
}
