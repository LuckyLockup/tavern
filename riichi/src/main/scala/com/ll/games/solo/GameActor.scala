package com.ll.games.solo

import akka.persistence.PersistentActor
import com.ll.domain.games.GameId
import com.ll.domain.games.persistence._
import com.ll.domain.games.solo.persistence.GameState
import com.ll.domain.ws.OutEventConverter
import com.ll.utils.Logging
import com.ll.ws.PubSub

class GameActor(gameId: GameId, pubSub: PubSub) extends PersistentActor with Logging {
  override def persistenceId = s"solo_${gameId.id.toString}"

  var _state = GameState(gameId)

  val receiveCommand: Receive = {
    case RiichiCmd.GetState(userId, _) =>
      _state.playerProjection(userId).foreach(stateForPlayer =>
        pubSub.sendToPlayer(userId, OutEventConverter.convert(stateForPlayer))
      )

    case cmd: Cmd =>
      _state.validate(cmd) match {
        case Left(error) =>
          log.info(s"Error validating $cmd")
          cmd match {
            case playerCmd: RiichiCmd =>
              pubSub.sendToPlayer(playerCmd.userId,  OutEventConverter.convert(error))
            case _ =>
          }
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
      log.error(s"Unknown command $cmd")
  }

  val receiveRecover: Receive = {
    case st =>
      log.info(s"Recovery event is received: $st")
  }
}
