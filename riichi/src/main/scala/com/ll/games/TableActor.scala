package com.ll.games

import akka.persistence.PersistentActor
import com.ll.domain.persistence._
import com.ll.utils.Logging
import com.ll.ws.PubSub

import scala.reflect.ClassTag

class TableActor[C <: TableCmd: ClassTag, E <: TableEvent: ClassTag](
  table: TableState[C, E],
  pubSub: PubSub)
  extends PersistentActor with Logging {

  override def persistenceId = s"solo_${table.tableId.id.toString}"

  var _state = table

  val receiveCommand: Receive = {
    case message => {
      try {
        message match {
          case cmd: TableCmd => ???
          case cmd: UserCmd =>
            sender()
            receiveUserCmd(cmd)
          case cmd: GameCmd => ???
          case cmd: C => ???
          case cmd => log.error(s"Unknown command $cmd")
        }
      } catch {
        case ex: Exception =>
          log.error("W")
          sender ! akka.actor.Status.Failure(ex)
          throw ex
      }
    }
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
