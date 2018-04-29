package com.ll.games

import akka.actor.{ActorRef, ActorSystem, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy}
import cats.Monad
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.TableId
import com.ll.utils.Logging
import com.ll.ws.PubSub
import akka.pattern.{Backoff, BackoffSupervisor}
import akka.util.Timeout
import com.ll.config.ServerConfig
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.riichi.{NoGameOnTable, RiichiTableState}
import com.ll.domain.ws.WsMsgIn.{GameCmd, TableCmd, UserCmd}
import com.ll.domain.ws.WsMsgOut

import scala.concurrent.duration._

class TablesService(pubSub: PubSub, config: ServerConfig)(implicit system: ActorSystem) extends Logging {
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(config.defaultTimeout)

  private var tables: Map[TableId, ActorRef] = Map.empty[TableId, ActorRef]

  def getOrCreate(tableId: TableId, userId: UserId): Unit = {
    tables.get(tableId) match {
      case Some(ar) =>
        log.info("Table is already created")
        ar ! UserCmd.GetState(tableId, userId)
      case None =>
        log.info(s"Creating table for $tableId")
        val table: RiichiTableState = NoGameOnTable(User(userId, "God"), tableId)
        val props: Props = Props(new TableActor[Riichi, RiichiTableState](table, pubSub))
        val supervisor = BackoffSupervisor.props(
          Backoff.onStop(
            props,
            childName = s"table_${tableId.id}",
            minBackoff = 3.seconds,
            maxBackoff = 30.seconds,
            randomFactor = 0.2 // adds 20% "noise" to vary the intervals slightly
          ).withSupervisorStrategy(
            OneForOneStrategy() {
              case ex  =>
                log.error(s"Exception in table actor: ${ex.getMessage}", ex)
                SupervisorStrategy.Resume
            })
        )

        val actorRef = system.actorOf(supervisor, name = s"supervisor_${table.tableId.id}")
        tables += tableId -> actorRef
        system.scheduler.scheduleOnce(30 minutes) {
          log.info(s"Stopping table $tableId")
          actorRef ! PoisonPill
          tables -= tableId
        }
        actorRef ! UserCmd.GetState(tableId, userId)
    }
  }

  def sendToGame(cmd: TableCmd): Unit = {
    if (tables.get(cmd.tableId).isEmpty) {
      log.warn(s"Message is sent to non existing tabled: $cmd")
      val userIdOpt = cmd match {
        case cmd: UserCmd => Some(cmd.userId)
        case cmd: GameCmd[_] => cmd.position.flatMap{
          case Left(userId) => Some(userId)
          case Right(_) => None
        }
        case _ => None
      }
      userIdOpt.foreach(userId => pubSub.sendToUser(userId, WsMsgOut.ValidationError(s"${cmd.tableId} doesn't exist")))
    }
    tables.get(cmd.tableId).foreach(ar => ar ! cmd)

  }

  def gamesCount: Int = tables.size
}

object TablesService {
  def apply[F[_] : Monad](system: ActorSystem, pubSub: PubSub, config: ServerConfig) =
    new TablesService(pubSub, config)(system)
}
