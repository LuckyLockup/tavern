package com.ll.games

import akka.actor.Cancellable
import akka.persistence.PersistentActor
import com.ll.ai.AIService
import com.ll.domain.ai.ServiceId
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.Player.{AIPlayer, HumanPlayer}
import com.ll.domain.games.{CommandEnvelop, GameType, ScheduledCommand}
import com.ll.domain.persistence._
import com.ll.domain.ws.WsMsgIn.{SpectacularCmd, WsGameCmd}
import com.ll.domain.ws.{WsMsgInProjector, WsMsgOut, WsMsgOutProjector}
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
    case env@CommandEnvelop(wsCmd, user) =>
      import WsMsgInProjector._
      //all commands in websocket must come from the player, so position must be always presented
      val cmdV: Either[WsMsgOut.ValidationError, TableCmd[GT]] = for {
        position <- _table.getPosition(user)
      } yield wsCmd.projection(position)
      cmdV match {
        case Right(cmd)  => processCmd(cmd, user)
        case Left(error) => pubSub.send(Some(user), error)
      }
    case cmd: TableCmd[GT]               => processCmd(cmd, None)
  }

  def processCmd(cmd: TableCmd[GT], senderId: Option[Either[ServiceId, User]]) = {
    try {
      log.info(s"${table.tableId} Processing $cmd from $senderId")
      cmd match {
        case cmd: TableCmd.GetState[GT] =>
          val state = _table.projection(cmd.position)
          pubSub.send(senderId, state)

        case SpectacularCmd.JoinAsSpectacular(_) =>
          senderId.foreach {
            case Right(humanUser) =>
              spectaculars += humanUser
              pubSub.sendToUsers(allUsers, WsMsgOut.SpectacularJoinedTable(humanUser, tableId))
            case Left(serviceId)  => ???
          }

        case SpectacularCmd.LeftAsSpectacular(_) =>
          senderId.foreach {
            case Right(humanUser) =>
              spectaculars -= humanUser
              pubSub.sendToUsers(allUsers, WsMsgOut.SpectacularLeftTable(humanUser, tableId))
            case Left(serviceId)  => ???
          }
        case cmd: WsGameCmd[GT]                  =>
          _table.validateCmd(cmd) match {
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
                Eff.dispatch(event)
              }
            }
          }
      }
    } catch {
      case ex: Exception =>
        log.error("W")
        sender ! akka.actor.Status.Failure(ex)
        throw ex
    }
  }

  val receiveRecover: Receive = {
    case st =>
      log.info(s"Recovery event is received: $st")
  }

  object Eff {
    import WsMsgOutProjector._

    def dispatch(event: TableEvent[GT]) = {
      dispatchToHumans(event)
      dispatchToAi(event)
    }

    def dispatchToHumans(ev: TableEvent[GT]) = {
      val spectacularEvents = spectaculars
        .map(user => (user.id, ev.projection(None)))

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
