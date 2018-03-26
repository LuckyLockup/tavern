package com.ll.ws

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Status}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source}
import cats.Monad
import com.ll.domain.auth.UserId
import com.ll.utils.Logging
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import org.reactivestreams.Publisher
import akka.stream.scaladsl.Sink

import scala.concurrent.Future

class PubSub[+F[_]]()(implicit system: ActorSystem, mat: Materializer) extends Logging {
  implicit val ec = system.dispatcher

  private var wsConnections: Map[UserId, ActorRef] = Map.empty[UserId, ActorRef]

  def getConnections = wsConnections.size

  def openNewConnection(id: UserId): Flow[Message, Message, NotUsed] = {
    closeConnection(id)

    val (actorRef: ActorRef, publisher: Publisher[TextMessage.Strict]) = Source.actorRef[WsMsg.Out](16, OverflowStrategy.fail)
      .map(msg => TextMessage.Strict(Codec.encodeWsMsg(msg)))
      .toMat(Sink.asPublisher(false))(Keep.both).run()

    val sink: Sink[Message, Unit] = Flow[Message]
      .watchTermination()((_, ft) => ft.foreach { _ => closeConnection(id) })
      .mapConcat {
        case TextMessage.Strict(msg) =>
          Codec.decodeWsMsg(msg).fold(err => {
            log.info(s"Unknown message $msg. Parsing error: $err")
            Nil
          }, msg => List(msg))
        case _                       =>
          Nil
      }
      .mapAsync(4) {
        case WsMsg.In.Ping(n) =>
          actorRef ! WsMsg.Out.Pong(n)
          Future.successful("Pong!")
        case msg              =>
          //TODO send to dispatcher
          Future.successful {
            log.info(s"Dispatching $msg")
          }
      }
      .to(Sink.ignore)

    log.info(s"Opening WS connection for $id")
    wsConnections += id -> actorRef
    Flow.fromSinkAndSource(sink, Source.fromPublisher(publisher))
  }

  def sendToPlayer(id: UserId, msg: WsMsg.Out) = wsConnections.get(id).foreach(ar => ar ! TextMessage(msg.toString))

  def closeConnection(id: UserId) = {
    wsConnections.get(id).foreach { ar =>
      log.info(s"Closing WS connection for $id")
      ar ! Status.Success("Done")
    }
    wsConnections -= id
  }
}

object PubSub {
  var _pubSub: Option[PubSub[Any]] = None

  def apply[F[_] : Monad](system: ActorSystem, mat: Materializer) = {
    val pubSub = new PubSub[F]()(system, mat)
    _pubSub = Some(pubSub)
    pubSub
  }

  def sendToPlayer(id: UserId, msg: WsMsg.Out) = _pubSub.foreach(ps => ps.sendToPlayer(id, msg))
}