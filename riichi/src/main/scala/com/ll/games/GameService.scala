package com.ll.games

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import cats.Monad
import com.ll.domain.auth.UserId
import com.ll.domain.games.GameId
import com.ll.domain.games.persistence.Cmd
import com.ll.domain.ws.{CmdConverter, WsMsg}
import com.ll.games.solo.GameActor
import com.ll.utils.Logging
import com.ll.ws.PubSub

import scala.concurrent.duration._

class GameService(pubSub: PubSub)(implicit system: ActorSystem) extends Logging {
  implicit val ec = system.dispatcher

  private var games: Map[GameId, ActorRef] = Map.empty[GameId, ActorRef]

  def getOrCreate(gameId: GameId) = {
    if (games.contains(gameId)) {
      log.info("Game is already created")
    } else {
      log.info(s"Creating game for $gameId")
      val actorRef = system.actorOf(Props(new GameActor(gameId, pubSub)))
      games += gameId -> actorRef
      system.scheduler.scheduleOnce(30 minutes) {
        log.info(s"Killing game $gameId")
        actorRef ! PoisonPill
        games -= gameId
      }
    }
  }

  def sendToGame(cmd: Cmd) = games.get(cmd.gameId).foreach(ar => ar ! cmd)

  def sendToGame(wsCmd: WsMsg.GameCmd, userId: UserId) = games
    .get(wsCmd.gameId)
    .foreach(ar => {
      CmdConverter.convert(wsCmd, userId) match {
        case Some(cmd) => ar! cmd
        case None =>
      }
    })

  def gamesCount: Int = games.size
}

object GameService {
  def apply[F[_] : Monad](system: ActorSystem, pubSub: PubSub) = new GameService(pubSub)(system)
}
