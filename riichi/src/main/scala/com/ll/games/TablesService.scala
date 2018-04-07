package com.ll.games

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import cats.Monad
import com.ll.domain.auth.UserId
import com.ll.domain.games.{GameId, TableId}
import com.ll.domain.messages.WsMsg
import com.ll.domain.messages.WsMsg.Out.Table
import com.ll.games.solo.TableActor
import com.ll.utils.Logging
import com.ll.ws.PubSub
import com.ll.domain.messages.WsMsg.Out.Table.TableState
import com.ll.domain.persistence.{TableCmd, UserCmd}
import akka.pattern.ask
import akka.util.Timeout
import com.ll.config.ServerConfig
import com.ll.domain.games.riichi.RiichiTable

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
        val table = RiichiTable(tableId)
        val actorRef = system.actorOf(Props(new TableActor(table, pubSub)))
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
