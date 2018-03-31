package com.ll.games.riichi

import akka.actor.Props
import akka.persistence.PersistentActor
import com.ll.domain.games.GameId
import com.ll.domain.games.persistence._
import com.ll.domain.games.riichi.persistence.GameState
import com.ll.domain.ws.OutEventConverter
import com.ll.utils.Logging
import com.ll.ws.PubSub

class GameActor(gameId: GameId, pubSub: PubSub) extends PersistentActor with Logging {
  override def persistenceId = s"riichi_${gameId.id.toString}"

  var _state = GameState(gameId)

  val receiveCommand: Receive = {
    case RiichiCmd.GetState(userId, _) =>
      val stateForPlayer =_state.playerProjection(userId)
      pubSub.sendToPlayer(userId, OutEventConverter.convert(stateForPlayer))

    case cmd: Cmd =>
      _state.validate(cmd) match {
        case Left(error) => log.info(s"Error validating $cmd")
        case Right(events) =>
          val (updatedState, accOutEvents) =events.foldLeft((_state, Nil : List[OutEvent])){
            case ((state, outEvents), event) =>
              val (newState, newEvents) = state.applyEvent(event)
              (newState, outEvents ::: newEvents)
          }
          _state = updatedState

          accOutEvents.foreach{
            case out: GeneralEvent =>
              pubSub.sendToPlayers(_state.players, OutEventConverter.convert(out))
            case out: PlayerEvent =>
              pubSub.sendToPlayer(out.userId, OutEventConverter.convert(out))
          }

      }
    case cmd =>
      log.error(s"Unkown command $cmd")
  }

  val receiveRecover: Receive = {
    case st =>
      log.info(s"Recovery event is received: $st")
  }
}
