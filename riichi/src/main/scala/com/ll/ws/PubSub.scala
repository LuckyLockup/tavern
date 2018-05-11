package com.ll.ws

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Status}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source}
import cats.Monad
import com.ll.domain.auth.{User, UserId}
import com.ll.utils.Logging
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import org.reactivestreams.Publisher
import akka.stream.scaladsl.Sink
import com.ll.domain.games.CommandEnvelop
import com.ll.domain.ws.{WsMsgCodec, WsMsgIn, WsMsgOut}
import com.ll.games.TablesService

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

class PubSub()(implicit system: ActorSystem, mat: Materializer) extends Logging {
  implicit val ec = system.dispatcher

  private val wsConnections: TrieMap[UserId, ActorRef] = TrieMap.empty[UserId, ActorRef]

  def getConnections = wsConnections.size

  def openNewConnection(user: User, tables: TablesService): Flow[Message, Message, NotUsed] = {
    wsConnections.get(user.id).foreach { ar =>
      log.info(s"Closing previous WS connection for ${user.id}")
      ar ! Status.Success("Done")
    }

    val (actorRef: ActorRef, publisher: Publisher[TextMessage.Strict]) = Source.actorRef[WsMsgOut](16, OverflowStrategy.fail)
      .map(msg => TextMessage.Strict(WsMsgCodec.encodeWsMsg(msg)))
      .toMat(Sink.asPublisher(false))(Keep.both).run()

    val sink: Sink[Message, Unit] = Flow[Message]
      .watchTermination()((_, ft) => ft.foreach { _ => {
        log.info(s"Closing WS connection from client for ${user.id}")
        wsConnections -= user.id
      }
      })
      .mapConcat {
        case TextMessage.Strict(msg) =>
          log.info(s"${user.id} <<< $msg")
          WsMsgCodec.decodeWsMsg(msg).fold(err => {
            log.info(s"Unknown message $msg. Parsing error: $err")
            actorRef ! WsMsgOut.ValidationError(s"Error parsing json message: $err")
            Nil
          }, msg => List(msg))
        case _                       =>
          Nil
      }
      .mapAsync(4) {
        case WsMsgIn.Ping(n)  =>
          actorRef ! WsMsgOut.Pong(n)
          Future.successful("Pong!")
        case msg: WsMsgIn.WsTableCmd =>
          tables.sendToGame(CommandEnvelop(msg, user))
          Future.successful {"Done"}
      }
      .to(Sink.ignore)

    log.info(s"Opening WS connection for ${user.id}")
    wsConnections += user.id -> actorRef
    Flow.fromSinkAndSource(sink, Source.fromPublisher(publisher))
  }

  def sendToUser(id: UserId, msg: WsMsgOut): Unit = wsConnections.get(id).foreach { ar =>
    log.info(s"$id >>> ${WsMsgCodec.encodeWsMsg(msg)}")
    ar ! msg
  }

  def sendToUsers(ids: Set[UserId], msg: WsMsgOut): Unit = {
    val validUsers = ids.intersect(wsConnections.keySet)
    val to = if (validUsers.size == 1) validUsers.map(_.id).mkString(",") else { validUsers.size.toString}
    log.info(s"[$to] >>> ${WsMsgCodec.encodeWsMsg(msg)}")
    validUsers.foreach { id =>
      wsConnections.get(id).foreach(ar => ar ! msg)
    }
  }
}

object PubSub {
  def apply[F[_] : Monad](system: ActorSystem, mat: Materializer) = new PubSub()(system, mat)
}