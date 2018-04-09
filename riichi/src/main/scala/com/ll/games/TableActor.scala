package com.ll.games

import akka.persistence.PersistentActor
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.{HumanPlayer, Player}
import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.persistence._
import com.ll.utils.Logging
import com.ll.ws.PubSub

import scala.reflect.ClassTag

class TableActor[C <: TableCmd: ClassTag, E <: TableEvent: ClassTag](
  table: TableState[C, E, _],
  pubSub: PubSub)
  extends PersistentActor with Logging {
  def tableId = table.tableId

  override def persistenceId = s"solo_${table.tableId.id.toString}"

  var _state = table
  var spectaculars: Set[User] = Set.empty[User]
  var humanPlayers: Set[HumanPlayer] = Set.empty[HumanPlayer]

  def allUsers: Set[UserId] = humanPlayers.map(_.userId) ++ spectaculars.map(_.id)

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
      sender() ! _state.projection(cmd)
      //TODO move this logic back to tableState.
    case UserCmd.JoinAsPlayer(_, user) =>
      if (humanPlayers.size < 4) {
        val newPlayer = HumanPlayer(user)
        humanPlayers += newPlayer
        pubSub.sendToUsers(allUsers, Out.Table.PlayerJoinedTable(newPlayer, tableId))
      } else {
        pubSub.sendToUser(user.id, Out.Message("You can't join table. Table is full."))
      }
    case UserCmd.LeftAsPlayer(_, user) =>
      humanPlayers.find(p => p.userId == user.id).foreach{p =>
        humanPlayers -= p
        pubSub.sendToUsers(allUsers, Out.Table.PlayerLeftTable(p, tableId))
      }
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
