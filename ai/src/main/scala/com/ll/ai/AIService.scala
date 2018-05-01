package com.ll.ai

import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.AIPlayer
import com.ll.domain.ws.WsMsgIn.{GameCmd, RiichiGameCmd}
import com.ll.domain.ws.WsMsgOut
import org.slf4j.LoggerFactory

import scala.concurrent.Future

case class AIService() {
  protected lazy val log = LoggerFactory.getLogger(this.getClass)

  def processEvent[GT <: GameType](
    aiPlayer: AIPlayer[GT],
    outEvent: WsMsgOut,
    state: WsMsgOut.TableState[GT]): Future[List[GameCmd[_]]] = {
    log.info(s"$aiPlayer received: $outEvent")
    (aiPlayer, state) match {
      case (riichiAi: AIPlayer[Riichi], riichiState: WsMsgOut.Riichi.RiichiTableState) =>
        processRiichiEvent(riichiAi, outEvent, riichiState)
    }
  }

  def processRiichiEvent(
    aiPlayer: AIPlayer[Riichi],
    outEvent: WsMsgOut,
    state: WsMsgOut.TableState[Riichi]): Future[List[GameCmd[Riichi]]] = {
    outEvent match {
      case WsMsgOut.Riichi.TileFromWallTaken(tableId, gameId, tile, turn, aiPlayer.position, _) =>
        Future.successful {
          Thread.sleep(1000)
          List(RiichiGameCmd.DiscardTile(tableId, gameId, tile, turn + 1, Some(Right(aiPlayer.position))))
        }
      case _                                                                            => Future.successful(Nil)
    }
  }
}
