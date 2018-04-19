package com.ll.ai

import com.ll.domain.games.GameType
import com.ll.domain.games.Player.Riichi.AIPlayer
import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.messages.WsMsg.Out.{GameEvent, TableState}
import com.ll.domain.persistence._
import org.slf4j.LoggerFactory

import scala.concurrent.Future

case class AIService() {
  protected lazy val log = LoggerFactory.getLogger(this.getClass)

  def processEvent[GT <: GameType](
    aiPlayer: AIPlayer[GT],
    outEvent: Out,
    state: TableState[GT]): Future[List[GameCmd[GT]]] = {
    log.info(s"$aiPlayer received: $outEvent")
    outEvent match {
      case _ =>
    }
    Future.successful(Nil)
  }
}
