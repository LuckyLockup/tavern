package com.ll.infra

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import cats.effect.Effect
import com.ll.domain.auth.UserId
import com.ll.utils.Logging

/**
  * https://stackoverflow.com/questions/41316173/akka-websocket-how-to-close-connection-by-server
  */
class PubSub[F[_]: Effect](system: ActorSystem) extends Logging {

  def openConnection(id: UserId): Flow[String, Nothing, NotUsed] = {
    val chatRef = system.actorOf(Props[WsActor])

    val sink = Flow[String]
      .map(msg => WsActor.SignedMessage(id, msg))
      .to(Sink.actorRef(chatRef, WsActor.CloseConnection(id)))

    val source: Source[Nothing, Unit] = Source.actorRef(16, OverflowStrategy.fail)
      .mapMaterializedValue {
        actor : ActorRef => {
          chatRef ! WsActor.OpenConnection(actor, id)
        }
      }

    Flow.fromSinkAndSource(sink, source)
  }

  def closeConnection(id: UserId) = ???

  def sendToPlayer(id: UserId, msg: Ws.Out) = ???

  def initialize(): PubSub[F] = {

    this
  }
}

object PubSub {
  def initialize[F[_]: Effect](system: ActorSystem, mat: Materializer): PubSub[F] =
    new PubSub[F](system).initialize()(mat)
}