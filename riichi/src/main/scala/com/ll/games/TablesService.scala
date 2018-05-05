package com.ll.games

import akka.actor.{ActorRef, ActorSystem, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy}
import cats.Monad
import com.ll.domain.auth.User
import com.ll.domain.games.{CommandEnvelop, TableId}
import com.ll.utils.Logging
import com.ll.ws.PubSub
import akka.pattern.{Backoff, BackoffSupervisor}
import akka.util.Timeout
import com.ll.ai.AIService
import com.ll.config.ServerConfig
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.riichi.{NoGameOnTable, RiichiTableState}
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.WsMsgOut

import scala.concurrent.duration._

class TablesService(pubSub: PubSub, config: ServerConfig, aIService: AIService)(implicit system: ActorSystem) extends Logging {
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(config.defaultTimeout)

  private var tables: Map[TableId, ActorRef] = Map.empty[TableId, ActorRef]

  def getOrCreate(tableId: TableId, user: User): Unit = {
    tables.get(tableId) match {
      case Some(ar) =>
        log.info("Table is already created")
        ar ! CommandEnvelop(WsRiichiCmd.GetState(tableId), Right(user))
      case None =>
        log.info(s"Creating table for $tableId")
        val table: RiichiTableState = NoGameOnTable(user, tableId)
        val props: Props = Props(new TableActor[Riichi, RiichiTableState](table, pubSub, aIService))
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
        actorRef ! CommandEnvelop(WsRiichiCmd.GetState(tableId), Right(user))
    }
  }

  def sendToGame(env: CommandEnvelop): Unit = {
    if (tables.get(env.tableId).isEmpty) {
      log.warn(s"Message is sent to non existing tabled: ${env.tableId}")
      val error = WsMsgOut.ValidationError(s"${env.cmd.tableId} doesn't exist")
      pubSub.send(env.senderId, error)
    }
    tables.get(env.tableId).foreach(ar => ar ! env)
  }

  def gamesCount: Int = tables.size
}

object TablesService {
  def apply[F[_] : Monad](system: ActorSystem, pubSub: PubSub, config: ServerConfig, aIService: AIService) =
    new TablesService(pubSub, config, aIService)(system)
}
