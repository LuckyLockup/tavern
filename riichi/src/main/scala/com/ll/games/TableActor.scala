package com.ll.games

import akka.actor.Cancellable
import akka.persistence.PersistentActor
import com.ll.ai.AIService
import com.ll.domain.ai.ServiceId
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.Player.{AIPlayer, HumanPlayer}
import com.ll.domain.games.{CommandEnvelop, GameType, ScheduledCommand}
import com.ll.domain.messages.WsMsgProjector
import com.ll.domain.persistence._
import com.ll.domain.ws.WsMsgIn.{GameCmd, GetState, JoinLeftCmd, PlayerCmd, SpectacularCmd}
import com.ll.domain.ws.WsMsgOut
import com.ll.utils.Logging
import com.ll.ws.PubSub

import scala.reflect.ClassTag

class TableActor[GT <: GameType, S <: TableState[GT, S] : ClassTag](
  table: TableState[GT, S],
  pubSub: PubSub,
  aiService: AIService)
  extends PersistentActor with Logging {

  def tableId = table.tableId

  override def persistenceId = s"solo_${table.tableId.id.toString}"

  implicit val ec = context.dispatcher

  var _table = table
  var spectaculars: Set[User] = Set.empty[User]
  var scheduledCommand: Option[Cancellable] = None

  def allUsers: Set[UserId] = {
    _table.players.collect { case p: HumanPlayer[GT] => p }.map(_.user.id) ++ spectaculars.map(_.id)
  }

  val receiveCommand: Receive = {
    case env@CommandEnvelop(cmd, user) => {
      try {
        log.info(s"${table.tableId} Processing $cmd from $sender")
        cmd match {
          case cmd: GetState =>
            val position = _table.getPosition(env.senderId).toOption
            val state = _table.projection(position)
            pubSub.send(env.senderId, state)

          case SpectacularCmd.JoinAsSpectacular(_) =>
            user.foreach { humanUser =>
              spectaculars += humanUser
              pubSub.sendToUsers(allUsers, WsMsgOut.SpectacularJoinedTable(humanUser, tableId))
            }

          case SpectacularCmd.LeftAsSpectacular(_) =>
            user.foreach { humanUser =>
              spectaculars -= humanUser
              pubSub.sendToUsers(allUsers, WsMsgOut.SpectacularLeftTable(humanUser, tableId))
            }

          case cmd: JoinLeftCmd =>
            _table.joinLeftCmd(cmd, user) match {
              case Right((msg, st)) =>
                _table = st
                pubSub.sendToUsers(allUsers, msg)
              case Left(error)      =>
                pubSub.send(env.senderId, error)
            }

          case cmd: GameCmd[GT]   =>
            val eventsV = _table.gameCmd(cmd)
            Eff.applyEvents(eventsV, env.senderId)
          case cmd: PlayerCmd[GT] =>
            val eventsV = for {
              position <- _table.getPosition(env.senderId)
              events <- _table.playerCmd(cmd, position)
            } yield events
            Eff.applyEvents(eventsV, env.senderId)
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

    def applyEvents(
      eventsV: Either[WsMsgOut.ValidationError, List[TableEvent[GT]]],
      senderId: Either[ServiceId, UserId]) = {

      eventsV match {
        case Left(error)   => pubSub.send(senderId, error)
        case Right(events) => persistAll(events) { e =>
          events.foreach { event =>
            val (cmds, newState) = _table.applyEvent(e)
            _table = newState
            cmds.foreach {
              case ScheduledCommand(duration, sccmd) =>
                val nextCmd = context.system.scheduler.scheduleOnce(duration, self, sccmd)
                scheduledCommand.foreach(_.cancel())
                scheduledCommand = Some(nextCmd)
            }
            dispatch(event)
          }
        }
      }
    }

    def dispatch(event: TableEvent[GT]) = {
      dispatchToHumans(event)
      dispatchToAi(event)
    }

    def dispatchToHumans(ev: TableEvent[GT]) = {
      val spectacularEvents = spectaculars
        .map(user => (user.id, ev.projection()))

      val playerEvents = table.players.collect { case p: HumanPlayer[GT] => p }
        .map(player => (player.user.id, ev.projection(Some(player.position))))

      (spectacularEvents ++ playerEvents)
        .groupBy(_._2)
        .map {
          case (event, set) => (event, set.map(_._1))
        }
        .foreach {
          case (event, ids) => pubSub.sendToUsers(ids, event)
        }
    }

    private def dispatchToAi(ev: TableEvent[GT]) = {
      //TODO refactor into one for each.
      table.players.collect { case p: AIPlayer[GT] => p }.foreach { ai =>
        aiService.processEvent(
          ai,
          ev.projection(Some(ai.position)),
          table.projection(Some(ai.position))
        ).map { cmds =>
          cmds.foreach { cmd =>
            self ! cmd
          }
        }
      }
    }
  }
}
