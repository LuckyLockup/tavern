package com.ll.domain.games.persistence

import com.ll.domain.auth.UserId
import com.ll.domain.games.GameId

sealed trait Cmd {
  def gameId: GameId
}

trait GameCmd extends Cmd

trait RiichiCmd extends Cmd {
  def userId: UserId
}

object GameCmd {
  case class JoinGameAsPlayer(userId: UserId, gameId: GameId) extends GameCmd
  case class StartGame(gameId: GameId) extends GameCmd
}

object RiichiCmd {
  case class GetState(userId: UserId, gameId: GameId) extends RiichiCmd
  case class DiscardTile(userId: UserId, gameId: GameId, tile: String) extends RiichiCmd
  case class GetTileFromWall(userId: UserId, gameId: GameId) extends RiichiCmd
}
