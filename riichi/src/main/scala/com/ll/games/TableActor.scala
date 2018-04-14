package com.ll.games

import akka.persistence.PersistentActor
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.Player.{AIPlayer, HumanPlayer}
import com.ll.domain.games.PlayerPosition
import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.messages.WsMsg.Out.ValidationError
import com.ll.domain.messages.WsMsgProjector
import com.ll.domain.persistence._
import com.ll.utils.Logging
import com.ll.ws.PubSub

import scala.reflect.ClassTag

class TableActor[
  P <: PlayerPosition,
  C <: TableCmd : ClassTag,
  E <: GameEvent[P] : ClassTag,
  S <: TableState[P, C, E, S] : ClassTag
  ](table: TableState[P, C, E, S], pubSub: PubSub)
  extends PersistentActor with Logging {
  def tableId = table.tableId

  override def persistenceId = s"solo_${table.tableId.id.toString}"

  var _table = table
  var spectaculars: Set[User] = Set.empty[User]

  def allUsers: Set[UserId] = {
    _table.humanPlayers.map(_.user.id) ++ spectaculars.map(_.id)
  }

  val receiveCommand: Receive = {
    case message => {
      try {
        log.info(s"${table.tableId} Processing $message")
        message match {
          case cmd: UserCmd.GetState              =>
            val state = _table.projection(cmd)
            sender() ! _table.projection(cmd)
            pubSub.sendToUser(cmd.userId, state)
          case cmd: UserCmd.JoinAsPlayer          =>
            persistAndNotifyClients(_table.joinGame(cmd), cmd.userId)
          case cmd: UserCmd.LeftAsPlayer          =>
            persistAndNotifyClients(_table.leftGame(cmd), cmd.userId)
          case UserCmd.JoinAsSpectacular(_, user) =>
            spectaculars += user
            pubSub.sendToUsers(allUsers, Out.Table.SpectacularJoinedTable(user, tableId))
          case UserCmd.LeftAsSpectacular(_, user) =>
            spectaculars -= user
            pubSub.sendToUsers(allUsers, Out.Table.SpectacularJoinedTable(user, tableId))
          //Game commands are special, because they can be sent to AI
          case cmd: C =>
            _table.validateCmd(cmd) match {
              case Left(error) => pubSub.sendToUser(cmd.userId, error)
              case Right(events) => persistAll(events) { e =>
                _table.getPlayer(e.position) match {
                  case None => log.warn(s"For $cmd player is not found")
                  case Some(humanPlayer: HumanPlayer) =>
                  case Some(aiPlayer: AIPlayer) =>
                }
                ???
              }
            }
          case cmd    => log.error(s"Unknown command $cmd")
        }
      } catch {
        case ex: Exception =>
          log.error("W")
          sender ! akka.actor.Status.Failure(ex)
          throw ex
      }
    }
  }

  private def persistAndNotifyClients(result: Either[ValidationError, (TableEvent, S)], userId: UserId) = result match {
    case Left(error)       => pubSub.sendToUser(userId, error)
    case Right((e, state)) => persist(e) { e =>
      _table = state
      pubSub.sendToUsers(allUsers, WsMsgProjector.convert(e, state))
    }
  }

  val receiveRecover: Receive = {
    case st =>
      log.info(s"Recovery event is received: $st")
  }
}
