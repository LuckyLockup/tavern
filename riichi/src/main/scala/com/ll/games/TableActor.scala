package com.ll.games

import akka.persistence.PersistentActor
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.{HumanPlayer, Player}
import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.messages.WsMsgProjector
import com.ll.domain.persistence._
import com.ll.utils.Logging
import com.ll.ws.PubSub

import scala.reflect.ClassTag

class TableActor[C <: TableCmd: ClassTag, E <: TableEvent: ClassTag, S <: TableState[C, E, S]: ClassTag](
  table: TableState[C, E, S],
  pubSub: PubSub)
  extends PersistentActor with Logging {
  def tableId = table.tableId

  override def persistenceId = s"solo_${table.tableId.id.toString}"

  var _table = table
  var spectaculars: Set[User] = Set.empty[User]

  def allUsers: Set[UserId] = {
    _table.players.collect{case p: HumanPlayer => p}.map(_.userId) ++ spectaculars.map(_.id)
  }

  val receiveCommand: Receive = {
    case message => {
      try {
        log.info(s"${table.tableId} Processing $message")
        message match {
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
      sender() ! _table.projection(cmd)
      //TODO move this logic back to tableState.
    case cmd: UserCmd.JoinAsPlayer =>
      _table.joinGame(cmd) match {
        case Left(error) => pubSub.sendToUser(cmd.userId, error)
        case Right((e, state)) => persist(e) { e =>
          _table = state
          pubSub.sendToUsers(allUsers, WsMsgProjector.convert(e, state))
        }
      }
    case UserCmd.LeftAsPlayer(_, user) =>

    case UserCmd.JoinAsSpectacular(_, user) =>
      spectaculars += user
      pubSub.sendToUsers(allUsers, Out.Table.SpectacularJoinedTable(user, tableId))
    case UserCmd.LeftAsSpectacular(_, user) =>
      spectaculars -= user
      pubSub.sendToUsers(allUsers, Out.Table.SpectacularJoinedTable(user, tableId))
  }

  val receiveRecover: Receive = {
    case st =>
      log.info(s"Recovery event is received: $st")
  }
}
