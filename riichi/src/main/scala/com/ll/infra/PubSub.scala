package com.ll.infra

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props, Status}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import cats.Monad
import com.ll.domain.auth.UserId
import com.ll.utils.Logging
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import org.reactivestreams.Publisher


class PubSub[+F[_]](system: ActorSystem, mat: Materializer) extends Logging {
  type FlowHandler = Flow[Message, Message, NotUsed]
  private implicit val _system = system
  private implicit val _mat = mat

  private var wsConnections: Map[UserId, ActorRef] = Map.empty[UserId, ActorRef]

  def getConnections = wsConnections.size

  def openNewConnection(id: UserId): Flow[Message, Message, NotUsed] = {
    closeConnection(id)

    val source: Source[Message, ActorRef] = Source.actorRef[Message](16, OverflowStrategy.fail)

    val (actorRef: ActorRef, publisher: Publisher[Message]) = Source.actorRef[Message](16, OverflowStrategy.fail)
      .toMat(Sink.asPublisher(false))(Keep.both).run()

    val sink: Sink[Message, NotUsed] = Flow[Message]
      .collect {
        case TextMessage.Strict(msg) ⇒
          log.info(s"Dispatching $msg")
          msg
      }
      //TODO decode
      //TODO send to dispatcher
      //TODO log not deserialized events
      .to(Sink.ignore)
    //TODO decrease the counter on disconnect
    //TODO add error strategy with logging and continue
//      .to(Sink.actorRef(actorRef, Status.Success("Done")))

    val flow: Flow[Message, Message, NotUsed] = Flow.fromSinkAndSource(sink, Source.fromPublisher(publisher))
    wsConnections += id -> actorRef
    flow
  }

  def sendToPlayer(id: UserId, msg: Ws.Out) = wsConnections.get(id).foreach(ar => ar ! TextMessage(msg.toString))

  def closeConnection(id: UserId) = {

    wsConnections.get(id).foreach { ar =>
      log.info(s"Closing $id")
      ar ! Status.Success("Done")
    }
    wsConnections -= id
  }

}

object PubSub {
  var _pubSub: Option[PubSub[Any]] = None

  def apply[F[_] : Monad](system: ActorSystem, mat: Materializer) = {
    val pubSub= new PubSub[F](system, mat)
    _pubSub = Some(pubSub)
    pubSub
  }

  def sendToPlayer(id: UserId, msg: Ws.Out) = _pubSub.foreach(ps => ps.sendToPlayer(id, msg))
}