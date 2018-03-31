package com.ll.domain.ws

import com.ll.domain.auth.UserId
import com.ll.domain.games.persistence.{Cmd, GameCmd, RiichiCmd}

object CmdConverter {
  def convert(msg: WsMsg.In, userId: UserId): Option[Cmd] = msg match {
    case WsMsg.In.JoinGameAsPlayer(gameId) =>
      Some(GameCmd.JoinGameAsPlayer(userId, gameId))
    case WsMsg.In.GetState(gameId) =>
      Some(RiichiCmd.GetState(userId, gameId))
    case WsMsg.In.DiscardTile(gameId, tile) =>
      Some(RiichiCmd.DiscardTile(userId, gameId, tile))
    case WsMsg.In.GetTileFromWall(gameId) =>
      Some(RiichiCmd.GetTileFromWall(userId, gameId))
    case _ => None
  }
}
