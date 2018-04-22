package com.ll.games

import akka.persistence.PersistentActor
import com.ll.ai.AIService
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.{GameType, ScheduledCommand}
import com.ll.domain.persistence._
import com.ll.domain.ws.WsMsgIn.{GameCmd, UserCmd}
import com.ll.domain.ws.WsMsgOut
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

  implicit val ec = context.dispatcher

  object services {
    implicit val ec = context.dispatcher
    val aiService = AIService()
    val dispatcher = new Dispatcher[GT, S](pubSub, aiService, self)
  }

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
            pubSub.sendToUsers(allUsers, WsMsgOut.SpectacularJoinedTable(user, tableId))
          case UserCmd.LeftAsSpectacular(_, user) =>
            spectaculars -= user
            pubSub.sendToUsers(allUsers, WsMsgOut.SpectacularJoinedTable(user, tableId))

          case cmd@UserCmd.JoinAsPlayer(_, user) =>
            _table.joinGame(cmd) match {
              case Left(error) => pubSub.sendToUser(user.id, error)
              case Right((event, newState)) =>
                persist(event) {e =>
                  _table = newState
                  services.dispatcher.dispatchEvent(_table, spectaculars, event)
                }
            }
          case cmd@UserCmd.LeftAsPlayer(_, user) =>
            _table.leftGame(cmd) match {
              case Left(error) => pubSub.sendToUser(user.id, error)
              case Right((event, newState)) =>
                persist(event) {e =>
                  _table = newState
                  services.dispatcher.dispatchEvent(_table, spectaculars, event)
                }
            }
          case cmd: GameCmd[GT] =>
            _table.validateCmd(cmd) match {
              case Left(error) => services.dispatcher.dispatchError(_table, error, cmd.position)
              case Right(events) => persistAll(events) { e =>
                events.foreach{ event =>
                  val (cmds, newState) = _table.applyEvent(e)
                  _table = newState
                  cmds.foreach{
                    case ScheduledCommand(duration, cmd) =>
                      context.system.scheduler.scheduleOnce(duration, self, cmd)
                  }
                  services.dispatcher.dispatchEvent(_table, spectaculars, event)
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
