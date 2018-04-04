package com.ll.domain.ws

import com.ll.domain.auth.UserId
import com.ll.domain.games.GameId

object  WsMsg {
  sealed trait In
  sealed trait GameCmd extends In {
    def gameId: GameId
  }

  object In {
    case class Ping(id: Int) extends In

    case class JoinGameAsPlayer(gameId: GameId) extends GameCmd
    case class GetState(gameId: GameId) extends GameCmd
    case class DiscardTile(gameId: GameId, tile: String) extends GameCmd
    case class GetTileFromWall(gameId: GameId) extends GameCmd
  }

  sealed trait Out
  object Out {
    case class Pong(id: Int) extends Out
    case class Text(txt: String) extends Out

    case class PlayerJoinedTheGame(userId: UserId) extends Out

    case class GameState(
      gameId: GameId,
      closedHand: List[String],
      openHand: List[String],
      currentTitle: Option[String],
      discard: List[String],
      turn: Int
    ) extends Out

    case class TileFromWall(tile: String) extends Out

    case class TileDiscarded(tile: String) extends Out

    case class ValidationError(error: String) extends Out

    case class GameWin() extends Out
    case class Loose() extends Out
  }
}
