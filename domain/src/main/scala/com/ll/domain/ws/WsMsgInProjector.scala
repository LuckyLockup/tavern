package com.ll.domain.ws

import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.persistence.TableCmd.RiichiCmd
import com.ll.domain.persistence.TableCmd
import com.ll.domain.ws.WsMsgIn.{WsRiichiCmd, WsTableCmd}

import scala.reflect.runtime.universe._

object WsMsgInProjector {
  implicit class TableProjector[GT <: GameType: TypeTag](wsMsgIn: WsTableCmd){
    def projection(position: PlayerPosition[GT]): TableCmd[GT] = {
      (wsMsgIn, position) match {
        case (msg: WsRiichiCmd, p: PlayerPosition[Riichi]) => riichiProjection(msg, p).asInstanceOf[TableCmd[GT]]
      }
    }
  }

  private def riichiProjection(msg: WsRiichiCmd, position: PlayerPosition[Riichi]): RiichiCmd = {
    msg match {
      case _ => ???
    }
  }
}
