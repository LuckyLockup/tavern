package com.ll.games.riichi

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import cats.Monad
import com.ll.domain.games.GameId
import com.ll.utils.Logging
import com.ll.ws.PubSub

import scala.concurrent.duration._

class Riichi(pubSub: PubSub)(implicit system: ActorSystem) extends Logging {
  implicit val ec = system.dispatcher

  private var games: Map[GameId, ActorRef] = Map.empty[GameId, ActorRef]

  def getOrCreate(gameId: GameId) = {
    log.info(s"Creating game for $gameId")
    val actorRef = system.actorOf(Props(new GameActor(gameId, pubSub)))
    games += gameId -> actorRef
    system.scheduler.scheduleOnce(20 seconds) {
      log.info(s"Killing game $gameId")
      actorRef ! PoisonPill
      games -= gameId
    }
  }

  def gamesCount: Int = games.size
}

object Riichi {
  def apply[F[_] : Monad](system: ActorSystem, pubSub: PubSub) = new Riichi(pubSub)(system)
}
