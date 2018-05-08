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
      case WsMsgOut.Riichi.TileFromWallTaken(tableId, gameId, turn, aiPlayer.position, tile, _) =>
        log.info(s"$aiPlayer discarding: $tile")
        Future.successful {
          List(RiichiCmd.DiscardTile(tableId, gameId, turn + 1, aiPlayer.position, tile))
        }
      case WsMsgOut.Riichi.TileDiscarded(tableId, gameId, turn, _, _, head :: tail) =>
        log.info(s"${aiPlayer.position} skipping action...")
        Future.successful(List(RiichiCmd.SkipAction(tableId, gameId, turn + 1, aiPlayer.position)))
      case _                                                                            => Future.successful(Nil)
    }
  }
}
