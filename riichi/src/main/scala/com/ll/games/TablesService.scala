package com.ll.games

import akka.actor.{ActorRef, ActorSystem, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy}
import cats.Monad
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.{GameId, TableId}
import com.ll.domain.messages.WsMsg.Out.Table
import com.ll.utils.Logging
import com.ll.ws.PubSub
import com.ll.domain.persistence._
import akka.pattern.{Backoff, BackoffSupervisor, ask}
import akka.util.Timeout
import com.ll.config.ServerConfig
import com.ll.domain.games.riichi.{NoGameOnTable, RiichiTableState}

import scala.concurrent.Future
import scala.concurrent.duration._

class TablesService(pubSub: PubSub, config: ServerConfig)(implicit system: ActorSystem) extends Logging {
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(config.defaultTimeout)

  private var tables: Map[TableId, ActorRef] = Map.empty[TableId, ActorRef]

  def getOrCreate(tableId: TableId, userId: UserId): Future[Table.TableState] = {
    tables.get(tableId)
      .map(ar => {
        log.info("Table is already created")
        (ar ? UserCmd.GetState(tableId, userId)).mapTo[Table.TableState]
      })
      .getOrElse {
        log.info(s"Creating table for $tableId")
        val table: RiichiTableState = NoGameOnTable(User(userId, "God"), tableId)
        val props: Props = Props(new TableActor[RiichiCmd, RiichiEvent, RiichiTableState](table, pubSub))
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
                log.error(s"Exception in table actor: ${ex.getMessage}")
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
        (actorRef ? UserCmd.GetState(tableId, userId)).mapTo[Table.TableState]
      }
  }

  def sendToGame(cmd: TableCmd): Unit = {
    if (tables.get(cmd.tableId).isEmpty) {
      log.warn(s"Message is sent to non existing tabled: $cmd")
    }
    tables.get(cmd.tableId).foreach(ar => ar ! cmd)

  }

  def gamesCount: Int = tables.size
}

object TablesService {
  def apply[F[_] : Monad](system: ActorSystem, pubSub: PubSub, config: ServerConfig) =
    new TablesService(pubSub, config)(system)
}
