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
import com.ll.domain.json.Codec
import com.ll.domain.messages.WsMsg
import com.ll.domain.persistence.TableCmd
import com.ll.games.TablesService

import scala.concurrent.Future

class PubSub()(implicit system: ActorSystem, mat: Materializer) extends Logging {
  implicit val ec = system.dispatcher

  private var wsConnections: Map[UserId, ActorRef] = Map.empty[UserId, ActorRef]

  def getConnections = wsConnections.size

  def openNewConnection(id: UserId, tables: TablesService): Flow[Message, Message, NotUsed] = {
    wsConnections.get(id).foreach { ar =>
      log.info(s"Closing previous WS connection for $id")
      ar ! Status.Success("Done")
    }

    val (actorRef: ActorRef, publisher: Publisher[TextMessage.Strict]) = Source.actorRef[WsMsg.Out](16, OverflowStrategy.fail)
      .map(msg => TextMessage.Strict(Codec.encodeWsMsg(msg)))
      .toMat(Sink.asPublisher(false))(Keep.both).run()

    val sink: Sink[Message, Unit] = Flow[Message]
      .watchTermination()((_, ft) => ft.foreach { _ => {
        log.info(s"Closing WS connection from client for $id")
        wsConnections -= id
      } })
      .mapConcat {
        case TextMessage.Strict(msg) =>
          log.info(s"$id <<< $msg")
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
        case msg: TableCmd    =>
          tables.sendToGame(msg)
          Future.successful {"Done"}
      }
      .to(Sink.ignore)

    log.info(s"Opening WS connection for $id")
    wsConnections += id -> actorRef
    Flow.fromSinkAndSource(sink, Source.fromPublisher(publisher))
  }

  def sendToUser(id: UserId, msg: WsMsg.Out): Unit = wsConnections.get(id).foreach { ar =>
    log.info(s"$id >>> ${Codec.encodeWsMsg(msg)}")
    ar ! msg
  }

  def sendToUsers(ids: Set[UserId], msg: WsMsg.Out): Unit = ids.foreach { id =>
    log.info(s"[${ids.size}] >>> ${Codec.encodeWsMsg(msg)}")
    wsConnections.get(id).foreach(ar => ar ! msg)
  }
}

object PubSub {
  def apply[F[_] : Monad](system: ActorSystem, mat: Materializer) = new PubSub()(system, mat)
}