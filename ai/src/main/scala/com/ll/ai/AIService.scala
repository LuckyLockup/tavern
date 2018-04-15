package com.ll.ai

import com.ll.domain.ai.AIType
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.messages.WsMsg.Out.GameEvent
import com.ll.domain.messages.WsMsg.Out.Riichi.RiichiState
import com.ll.domain.persistence.{RiichiCmd, RiichiEvent, TableCmd, TableEvent}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

case class AIService() {
  protected lazy val log = LoggerFactory.getLogger(this.getClass)

  def processEvent(
    ai: AIType[Riichi],
    position: PlayerPosition[Riichi],
    outEvent: GameEvent[Riichi],
    state: RiichiState): Future[List[TableCmd]] = {

    log.info(s"$ai <--- $outEvent")
    outEvent match {
      case _ =>
    }
    ???
  }
}
