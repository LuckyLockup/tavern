package com.ll.domain.ws

import com.ll.domain.ai.ServiceId
import com.ll.domain.auth.User
import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.riichi.RiichiConfig
import com.ll.domain.persistence.TableCmd.RiichiCmd
import com.ll.domain.persistence.TableCmd
import com.ll.domain.ws.WsMsgIn.{WsGameCmd, WsRiichiCmd, WsTableCmd}

import scala.reflect.runtime.universe._

object WsMsgInProjector {
  def projection[GT <: GameType](
    wsMsgIn: WsGameCmd[GT],
    position: PlayerPosition[GT]): TableCmd[GT] = {

    (wsMsgIn, position) match {
      case (msg: WsRiichiCmd, p: PlayerPosition[Riichi]) => riichiProjection(msg, p).asInstanceOf[TableCmd[GT]]
    }

  }

  def riichiProjection(wsMsg: WsRiichiCmd, position: PlayerPosition[Riichi]): RiichiCmd = {
    wsMsg match {
      case WsRiichiCmd.StartWsGame(tableId, gameId, configOpt) =>
        RiichiCmd.StartGame(tableId, gameId, configOpt.getOrElse(RiichiConfig()))
      case WsRiichiCmd.ClaimChow(tableId, gameId, turn, onTile, tiles) =>
        RiichiCmd.ClaimChow(tableId, gameId, turn, position, onTile, tiles)
      case WsRiichiCmd.ClaimPung(tableId, gameId, turn) =>
        RiichiCmd.ClaimPung(tableId, gameId, turn, position)
      case WsRiichiCmd.DeclareRon(tableId, gameId, turn, _) =>
        RiichiCmd.DeclareRon(tableId, gameId, turn, position, None)
      case WsRiichiCmd.DeclareTsumo(tableId, gameId, turn,  _) =>
        RiichiCmd.DeclareTsumo(tableId, gameId, turn, position, None)
      case WsRiichiCmd.DiscardTile(tableId, gameId, tile, turn) =>
        RiichiCmd.DiscardTile(tableId, gameId, turn, position, tile)
      case WsRiichiCmd.GetTileFromWall(tableId, gameId, turn) =>
        RiichiCmd.GetTileFromTheWall(tableId, gameId, turn, position)
      case WsRiichiCmd.SkipAction(tableId, gameId, turn) =>
        RiichiCmd.SkipAction(tableId, gameId, turn, position)
    }
  }
}
