package com.ll.domain.ws

import com.ll.domain.auth.UserId
import com.ll.domain.games.GameId
import com.ll.domain.games.persistence.OutEvent

object WsMsg {
  sealed trait In

  object In {
    case class Ping(id: Int) extends In
    case class Cmd(cmd: Cmd) extends In

    case class JoinGameAsPlayer(gameId: GameId) extends In
    case class GetState(gameId: GameId) extends In
    case class DiscardTile(gameId: GameId, tile: String) extends In
    case class GetTileFromWall(gameId: GameId) extends In
  }

  sealed trait Out
  object Out {
    case class Pong(id: Int) extends Out
    case class Text(txt: String) extends Out

    case class PlayerJoinedTheGame(id: UserId) extends Out

    case class GameState(
      gameId: GameId,
      closedHand: List[String],
      openHand: List[String],
      currentTitle: Option[String],
      discard: List[String]
    ) extends Out

    case class TileFromWall(tile: String) extends Out

    case class TileDiscarded(tile: String) extends Out

    case class ValidationError(error: String) extends Out
  }
}
