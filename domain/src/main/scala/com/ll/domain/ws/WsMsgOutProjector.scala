package com.ll.domain.ws

import com.ll.domain.Const
import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.persistence.TableCmd.RiichiCmd
import com.ll.domain.persistence.{RiichiEvent, TableEvent}
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import cats.implicits._
import com.ll.domain.ws.WsRiichi.WsDeclaredSet

object WsMsgOutProjector {
  implicit class TableProjector[GT <: GameType](event: TableEvent[GT]){
    def projection(position: Option[PlayerPosition[GT]]): Option[WsMsgOut] = {
      (event, position) match {
        case (ev: TableEvent[Riichi], p: Option[PlayerPosition[Riichi]]) => riichiProjection(ev, p)
      }
    }
  }

  private def riichiProjection(event: TableEvent[Riichi], position: Option[PlayerPosition[Riichi]] = None): Option[WsMsgOut] = {

    event match {
      case RiichiEvent.PlayerJoined(tableId, player) =>
        WsMsgOut.Riichi.PlayerJoinedTable(tableId, player).some

      case RiichiEvent.PlayerLeft(tableId, player) =>
        WsMsgOut.Riichi.PlayerLeftTable(tableId, player).some

      case RiichiEvent.GameStarted(tableId, gameId, config) =>
        WsMsgOut.Riichi.GameStarted(tableId, gameId, 1).some

      case RiichiEvent.GamePaused(tableId, gameId, turn)          =>
        WsMsgOut.Riichi.GamePaused(tableId, gameId, turn).some

      case RiichiEvent.TileDiscared(tableId, gameId, turn, pos, tile, cmds) =>
        val cmdsToPlayer = cmds.filter(cmd => position.contains(cmd.position)).flatMap(cmd => riichiCmdProjection(cmd))
        WsMsgOut.Riichi.TileDiscarded(tableId, gameId, turn, pos, tile.repr, cmdsToPlayer).some

      case RiichiEvent.TileFromTheWallTaken(tableId, gameId, turn, playerPosition, tile, cmds) =>
        if (position.contains(playerPosition)) {
          val cmdsToPlayer = cmds.filter(cmd => position.contains(cmd.position)).flatMap(cmd => riichiCmdProjection(cmd))
          WsMsgOut.Riichi.TileFromWallTaken(tableId, gameId, turn, playerPosition, tile.repr, cmdsToPlayer).some
        } else {
          WsMsgOut.Riichi.TileFromWallTaken(tableId, gameId, turn, playerPosition, Const.ClosedTile, Nil).some
        }

      case RiichiEvent.TsumoDeclared(tableId, gameId, turn, pos) =>
        WsMsgOut.Riichi.TsumoDeclared(tableId, gameId, turn, pos).some

      case RiichiEvent.GameScored(tableId, gameId, turn, gameScore) =>
        WsMsgOut.Riichi.GameScored(tableId, gameId, turn, gameScore).some

      case RiichiEvent.PungClaimed(tableId, gameId, turn, pos, from, claimedTile, tiles) =>
        WsMsgOut.Riichi.TileClaimed(
          tableId = tableId,
          gameId = gameId,
          setName = "pung",
          set = WsDeclaredSet(claimedTile.repr, tiles.map(_.repr), from, turn),
          position = pos
        ).some

      case RiichiEvent.ChowClaimed(tableId, gameId, turn, pos, from, claimedTile, tiles) =>
        WsMsgOut.Riichi.TileClaimed(
          tableId = tableId,
          gameId = gameId,
          setName = "chow",
          set = WsDeclaredSet(claimedTile.repr, tiles.map(_.repr), from, turn),
          position = pos
        ).some

      case RiichiEvent.ActionSkipped(tableId, gameId, turn, pos) =>
        if (position.contains(pos)){
          WsMsgOut.Riichi.ActionSkipped(tableId, gameId, turn).some
        } else {
          None
        }
    }
  }

  def riichiCmdProjection(cmd: RiichiCmd): Option[WsRiichiCmd] = cmd match {
    case RiichiCmd.DeclareRon(tableId, gameId, turn, position, handValue) =>
      WsRiichiCmd.DeclareRon(tableId, gameId, turn, handValue).some

    case RiichiCmd.DeclareTsumo(tableId, gameId, turn, position, handValue) =>
      WsRiichiCmd.DeclareTsumo(tableId, gameId, turn, handValue).some

    case RiichiCmd.ClaimChow(tableId, gameId, turn, position, tiles) =>
      WsRiichiCmd.ClaimChow(tableId, gameId, turn, tiles).some

    case RiichiCmd.ClaimPung(tableId, gameId, turn, position) =>
      WsRiichiCmd.ClaimPung(tableId, gameId, turn).some

    case _ => None

  }
}
