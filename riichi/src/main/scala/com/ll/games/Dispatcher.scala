package com.ll.games

import akka.actor.ActorRef
import com.ll.ai.AIService
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.GameType
import com.ll.domain.games.Player.Riichi.{AIPlayer, HumanPlayer}
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.messages.WsMsg.Out.ValidationError
import com.ll.domain.messages.WsMsgProjector
import com.ll.domain.persistence.{GameCmd, TableEvent, TableState}
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

  def dispatchError(
    table: TableState[GT, S],
    error: ValidationError,
    position: Option[Either[UserId, PlayerPosition[GT]]]
  ) = {
    position.foreach {
      case Left(userId) => pubSub.sendToUser(userId, error)
      case Right(p)     => table.getPlayer(p).foreach {
        case player: HumanPlayer[GT] => pubSub.sendToUser(player.user.id, error)
        case ai: AIPlayer[GT]        => aiService.processEvent(ai, error, table.projection(position))
          .map { cmds =>
            cmds.map {
              case gameCmd: GameCmd[GT] => gameCmd.updatePosition(Right(ai.position))
              case cmd                  => cmd
            }.foreach { cmd =>
              tableActorRef ! cmd
            }
          }
      }
    }
  }

  private def sendToAi(table: TableState[GT, S], ev: TableEvent[GT]) = {
    table.aiPlayers.foreach { ai =>
      aiService.processEvent(
        ai,
        ev.projection(Some(ai.position)),
        table.projection(Some(Right(ai.position)))
      ).map { cmds =>
        cmds.map {
          case gameCmd: GameCmd[GT] => gameCmd.updatePosition(Right(ai.position))
          case cmd                  => cmd
        }.foreach { cmd =>
          tableActorRef ! cmd
        }
      }

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
        case (event, userIds)              => pubSub.sendToUsers(userIds, event)
      }
  }
}
