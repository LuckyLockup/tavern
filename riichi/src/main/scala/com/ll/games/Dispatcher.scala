package com.ll.games

import akka.actor.ActorRef
import com.ll.ai.AIService
import com.ll.domain.auth.User
import com.ll.domain.games.GameType
import com.ll.domain.messages.WsMsgProjector
import com.ll.domain.persistence.{TableEvent, TableState}
import com.ll.utils.Logging
import com.ll.ws.PubSub

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

class Dispatcher[GT <: GameType, S <: TableState[GT, S] : ClassTag](
  pubSub: PubSub,
  aiService: AIService,
  tableActorRef: ActorRef
)(implicit ec: ExecutionContext) extends Logging {
  import WsMsgProjector._

  def dispatchEvent(
    table: TableState[GT, S],
    spectaculars: Set[User],
    event: TableEvent[GT]
  ) = {
    sendToAi(table, event)
    sendToHumans(table, event, spectaculars)
  }

  private def sendToAi(table: TableState[GT, S], ev: TableEvent[GT]) = {
    for {
      ai <- table.aiPlayers
      cmds <- aiService.processEvent(ai,
        ev.projection(Some(ai.position)),
        table.projection(Some(Right(ai.position))))
      cmd <- cmds
    } {
      tableActorRef ! cmd
    }
  }

  private def sendToHumans(table: TableState[GT, S], ev: TableEvent[GT], spectaculars: Set[User]) = {
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
        case (event, userIds)  => pubSub.sendToUsers(userIds, event)
      }
  }
}
