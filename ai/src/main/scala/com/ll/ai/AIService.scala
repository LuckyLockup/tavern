package com.ll.ai

import com.ll.domain.ai.AIType
import com.ll.domain.games.PlayerPosition
import com.ll.domain.games.riichi.RiichiOut
import com.ll.domain.messages.WsMsg.Out.Table
import com.ll.domain.persistence.{RiichiCmd, RiichiEvent, TableCmd, TableEvent}
import org.slf4j.LoggerFactory

import scala.concurrent.Future

case class AIService() {
  protected lazy val log = LoggerFactory.getLogger(this.getClass)

  def processEvent(
    ai: AIType,
    position: PlayerPosition,
    outEvent: RiichiOut,
    state: Table.TableState): Future[List[TableCmd]] = {

    log.info(s"$ai <--- $outEvent")
    outEvent match {
      case RiichiOut.TileFromWallTaken(_, position, Some(tile)) =>
           ???
      case _ =>
    }
    ???
  }
}
