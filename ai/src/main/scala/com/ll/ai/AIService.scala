package com.ll.ai

import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.Riichi.AIPlayer
import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.messages.WsMsg.Out.{TableState}
import com.ll.domain.persistence._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

case class AIService() {
  protected lazy val log = LoggerFactory.getLogger(this.getClass)

  def processEvent[GT <: GameType](
    aiPlayer: AIPlayer[GT],
    outEvent: Out,
    state: TableState[GT]): Future[List[GameCmd[_]]] = {
    log.info(s"$aiPlayer received: $outEvent")
    (aiPlayer, state) match {
      case (riichiAi: AIPlayer[Riichi], riichiState: TableState[Riichi]) =>
        processRiichiEvent(riichiAi, outEvent, riichiState)
    }
  }

  def processRiichiEvent(
    aiPlayer: AIPlayer[Riichi],
    outEvent: Out,
    state: TableState[Riichi]): Future[List[GameCmd[Riichi]]] = {
    outEvent match {
      case Out.Riichi.TileFromWallTaken(tableId, gameId, tile, turn, aiPlayer.position) =>
        Future.successful {
          Thread.sleep(1000)
          List(RiichiGameCmd.DiscardTile(tableId, gameId, tile, turn + 1, Some(Right(aiPlayer.position))))
        }
      case _                                                                            => Future.successful(Nil)
    }
  }
}
