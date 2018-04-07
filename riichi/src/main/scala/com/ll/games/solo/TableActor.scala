package com.ll.games.solo

import akka.persistence.PersistentActor
import com.ll.domain.games.{GameId, GameTable, TableId}
import com.ll.domain.messages.WsMsg.Out.Table
import com.ll.domain.messages.WsMsgProjector
import com.ll.domain.persistence._
import com.ll.utils.Logging
import com.ll.ws.PubSub

class TableActor[C <: TableCmd, E <: TableEvent, S <: GameState](table: GameTable[C, E, S], pubSub: PubSub)
  extends PersistentActor with Logging {

  override def persistenceId = s"solo_${table.tableId.id.toString}"

  var _state = table

  val receiveCommand: Receive = {
    case cmd: TableCmd => ???
    case cmd: UserCmd => receiveUserCmd(cmd)
    case cmd: GameCmd => ???
    case cmd: C => ???
//    case cmd: Cmd =>
//      _state.validate(cmd) match {
//        case Left(error) =>
//          log.info(s"Error validating $cmd")
//          cmd match {
//            case playerCmd: RiichiCmd =>
//              pubSub.sendToUser(playerCmd.userId,  WsMsgProjector.convert(error))
//            case _ =>
//          }
//        case Right(events) =>
//          val (updatedState, accOutEvents) =events.foldLeft((_state, Nil : List[OutEvent])){
//            case ((state, outEvents), event) =>
//              val (newState, newEvents) = state.applyEvent(event)
//              (newState, outEvents ::: newEvents)
//          }
//          _state = updatedState
//
//          accOutEvents.foreach{
//            case out: GeneralEvent =>
//              pubSub.sendToUsers(_state.players, WsMsgProjector.convert(out))
//            case out: PlayerEvent =>
//              pubSub.sendToUser(out.userId, WsMsgProjector.convert(out))
//          }
//
//      }
    case cmd =>
      log.error(s"Unknown command $cmd")
  }

  def receiveUserCmd: UserCmd => Unit = {
    case cmd: UserCmd.GetState =>
      sender() ! _state.projection(cmd)
  }

  val receiveRecover: Receive = {
    case st =>
      log.info(s"Recovery event is received: $st")
  }
}
