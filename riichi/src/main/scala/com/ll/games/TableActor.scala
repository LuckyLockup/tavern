package com.ll.games

import akka.persistence.PersistentActor
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.GameType
import com.ll.domain.games.Player.Riichi.{AIPlayer, HumanPlayer}
import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.messages.WsMsg.Out.ValidationError
import com.ll.domain.messages.WsMsgProjector
import com.ll.domain.persistence._
import com.ll.utils.Logging
import com.ll.ws.PubSub

import scala.reflect.ClassTag

class TableActor[
  GT <: GameType,
  S <: TableState[GT, S] : ClassTag
  ](table: TableState[GT, S], pubSub: PubSub)
  extends PersistentActor with Logging {
  def tableId = table.tableId

  override def persistenceId = s"solo_${table.tableId.id.toString}"

  var _table = table
  var spectaculars: Set[User] = Set.empty[User]

  def allUsers: Set[UserId] = {
    _table.playerIds ++ spectaculars.map(_.id)
  }

  val receiveCommand: Receive = {
    case message => {
      try {
        log.info(s"${table.tableId} Processing $message")
        message match {
          case cmd: UserCmd.GetState              =>
            val state = _table.projection(Some(Left(cmd.userId)))
            pubSub.sendToUser(cmd.userId, state)
          case UserCmd.JoinAsSpectacular(_, user) =>
            spectaculars += user
            pubSub.sendToUsers(allUsers, Out.Riichi.SpectacularJoinedTable(user, tableId))
          case UserCmd.LeftAsSpectacular(_, user) =>
            spectaculars -= user
            pubSub.sendToUsers(allUsers, Out.Riichi.SpectacularJoinedTable(user, tableId))

          case cmd@UserCmd.JoinAsPlayer(_, user) =>
            _table.joinGame(cmd) match {
              case Left(error) => pubSub.sendToUser(user.id, error)
              case Right((event, newState)) =>
                persist(event) {e =>
                  _table = newState
                  pubSub.sendToUsers(_table.playerIds, WsMsgProjector.convert(event, table))
                }
            }
          case cmd@UserCmd.LeftAsPlayer(_, user) =>
            _table.leftGame(cmd) match {
              case Left(error) => pubSub.sendToUser(user.id, error)
              case Right((event, newState)) =>
                persist(event) {e =>
                  _table = newState
                  pubSub.sendToUsers(_table.playerIds, WsMsgProjector.convert(event, table))
                }
            }
          case cmd: GameCmd[GT] =>
            _table.validateCmd(cmd) match {
              case Left(error) => cmd.position
              case Right(events) => persistAll(events) { e =>
                events.foreach{ event =>
                  _table = _table.applyEvent(e)
                  pubSub.sendToUsers(_table.playerIds, WsMsgProjector.convert(event, table))
                }
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


  val receiveRecover: Receive = {
    case st =>
      log.info(s"Recovery event is received: $st")
  }
}
