package com.ll.ai

import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.AIPlayer
import com.ll.domain.persistence.TableCmd
import com.ll.domain.persistence.TableCmd.RiichiCmd
import com.ll.domain.ws.WsMsgOut
import org.slf4j.LoggerFactory

import scala.concurrent.Future

case class AIService() {
  protected lazy val log = LoggerFactory.getLogger(this.getClass)

  def processEvent[GT <: GameType](
    aiPlayer: AIPlayer[GT],
    outEvent: WsMsgOut,
    state: WsMsgOut.TableState[GT]): Future[List[TableCmd[_]]] = {
    log.info(s"$aiPlayer received: $outEvent")
    (aiPlayer, state) match {
      case (riichiAi: AIPlayer[Riichi], riichiState: WsMsgOut.Riichi.RiichiTableState) =>
        processRiichiEvent(riichiAi, outEvent, riichiState)
    }
  }

  def processRiichiEvent(
    aiPlayer: AIPlayer[Riichi],
    outEvent: WsMsgOut,
    state: WsMsgOut.TableState[Riichi]): Future[List[TableCmd[Riichi]]] = {
    outEvent match {
      case WsMsgOut.Riichi.TileFromWallTaken(tableId, gameId, tile, turn, aiPlayer.position, _) =>
        Future.successful {
          Thread.sleep(1000)
          List(RiichiCmd.DiscardTile(tableId, gameId, tile, turn + 1, aiPlayer.position))
        }
      case _                                                                            => Future.successful(Nil)
    }
  }
}
