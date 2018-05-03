package com.ll.games

import akka.persistence.PersistentActor
import com.ll.ai.AIService
import com.ll.domain.ai.ServiceId
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.{CommandEnvelop, GameType, ScheduledCommand}
import com.ll.domain.messages.WsMsgProjector
import com.ll.domain.persistence._
import com.ll.domain.ws.WsMsgIn.{CommonCmd, GameCmd}
import com.ll.domain.ws.WsMsgOut
import com.ll.domain.ws.WsMsgOut.ValidationError
import com.ll.utils.Logging
import com.ll.ws.PubSub

import scala.reflect.ClassTag

class TableActor[
GT <: GameType,
S <: TableState[GT, S] : ClassTag
](
  table: TableState[GT, S],
  pubSub: PubSub,
  aiService: AIService)
  extends PersistentActor with Logging {

  def tableId = table.tableId

  override def persistenceId = s"solo_${table.tableId.id.toString}"

  implicit val ec = context.dispatcher

  var _table = table
  var spectaculars: Set[User] = Set.empty[User]

  def allUsers: Set[UserId] = {
    _table.playerIds ++ spectaculars.map(_.id)
  }

  val receiveCommand: Receive = {
    case env@CommandEnvelop(cmd, user) => {
      try {
        log.info(s"${table.tableId} Processing $cmd from $sender")
        cmd match {
          case cmd: CommonCmd.GetState =>
            val state = _table.projection(Some(env.sender))
            pubSub.send(env.sender, state)

          case CommonCmd.JoinAsSpectacular(_) =>
            user match {
              case Right(humanUser) =>
                spectaculars += humanUser
                pubSub.sendToUsers(allUsers, WsMsgOut.SpectacularJoinedTable(humanUser, tableId))
              case Left(_)          => ???
            }

          case CommonCmd.LeftAsSpectacular(_) =>
            user match {
              case Right(humanUser) =>
                spectaculars -= humanUser
                pubSub.sendToUsers(allUsers, WsMsgOut.SpectacularLeftTable(humanUser, tableId))
              case Left(_)          => ???
            }

          case cmd@CommonCmd.JoinAsPlayer(_) =>
            user match {
              case Right(humanUser) =>
                _table.joinGame(cmd, humanUser) match {
                  case Left(error)              => pubSub.send(env.sender, error)
                  case Right((event, newState)) =>
                    persist(event) { e =>
                      _table = newState
                      Eff.dispatch(event)
                    }
                }
              case Left(_)          => ???
            }

          case cmd@CommonCmd.LeftAsPlayer(_) =>
            user match {
              case Right(humanUser) =>
                _table.leftGame(cmd, humanUser.id) match {
                  case Left(error)              => pubSub.send(env.sender, error)
                  case Right((event, newState)) =>
                    persist(event) { e =>
                      _table = newState
                      Eff.dispatch(event)
                    }
                }
            }

          case cmd: GameCmd[GT] =>
            _table.validateCmd(cmd) match {
              case Left(error)   => pubSub.send(env.sender, error)
              case Right(events) => persistAll(events) { e =>
                events.foreach { event =>
                  val (cmds, newState) = _table.applyEvent(e)
                  _table = newState
                  cmds.foreach {
                    case ScheduledCommand(duration, sccmd) =>
                      context.system.scheduler.scheduleOnce(duration, self, sccmd)
                  }
                  Eff.dispatch(event)
                }
              }
            }
          case cmd              => log.error(s"Unknown command $cmd")
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

  object Eff {
    import WsMsgProjector._

    def dispatch(event: TableEvent[GT]) = {
      dispatchToHumans(event)
      dispatchToAi(event)
    }

    def dispatchToHumans(ev: TableEvent[GT]) = {
      val spectacularEvents = spectaculars
        .map(user => (user.id, ev.projection()))

      val playerEvents = table.humanPlayers
        .map(player => (player.user.id, ev.projection(Some(player.position))))

      (spectacularEvents ++ playerEvents)
        .groupBy(_._2)
        .map {
          case (event, set) => (event, set.map(_._1))
        }
        .foreach {
          case (event, ids) if ids.size == 1 => pubSub.sendToUser(ids.head, event)
          case (event, userIds)              => pubSub.sendToUsers(userIds, event)
        }
    }

    private def dispatchToAi(ev: TableEvent[GT]) = {
      table.aiPlayers.foreach { ai =>
        aiService.processEvent(
          ai,
          ev.projection(Some(ai.position)),
          table.projection(Some(Left(ai.serviceId)))
        ).map { cmds =>
          cmds.foreach { cmd =>
            self ! cmd
          }
        }
      }
    }
  }
}
