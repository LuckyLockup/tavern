package com.ll.games

import akka.persistence.PersistentActor
import com.ll.domain.games.Player
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
  var players: Set[Player] = Set.empty

  val receiveCommand: Receive = {
    case message => {
      try {
        message match {
          case cmd: TableCmd => receiveTableCmd(cmd)
          case cmd: UserCmd => receiveUserCmd(cmd)
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
      log.info("Returning state")
      log.info(s"Sender ${sender()}")
      sender() ! _state.projection(cmd)
  }

  def receiveTableCmd: TableCmd => Unit = {
    case _ => ???
  }

  val receiveRecover: Receive = {
    case st =>
      log.info(s"Recovery event is received: $st")
  }
}
