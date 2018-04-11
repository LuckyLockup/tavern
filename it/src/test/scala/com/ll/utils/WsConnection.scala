package com.ll.utils

import akka.actor.{ActorRef, ActorSystem, Status}
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest}
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source, _}
import akka.testkit.{TestKitBase, TestProbe}
import akka.{Done, NotUsed}
import com.ll.domain.auth.UserId
import com.ll.domain.json.Codec
import com.ll.domain.messages.WsMsg
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.reactivestreams.Publisher

import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

class WsConnection(userId: UserId, as: ActorSystem, mat: Materializer, http: HttpExt, config: TestConfig)
  extends TestKitBase with Logging {

  implicit lazy val system = as
  implicit lazy val materializer = mat

  log.info(s"Creating connection for $userId")

  implicit val ec = system.dispatcher

  val req = WebSocketRequest(uri = s"${config.wsUrl}/${userId.id}")

  val probe = TestProbe()

  val (ws: ActorRef, publisher: Publisher[TextMessage.Strict]) = Source
    .actorRef[WsMsg.In](16, OverflowStrategy.fail)
    .log(s"$userId sent: ")
    .map(msg => {
      val json = encodeWsMsg(msg)
      log.info(s"WS [${userId.id}] >>>>  $json")
      TextMessage.Strict(json)
    })
    .toMat(Sink.asPublisher(false))(Keep.both).run()

  val sink: Sink[Message, NotUsed] = Flow[Message]
    .mapAsync(1) {
      case TextMessage.Strict(msg) =>
        decodeWsMsg(msg) match {
          case Left(error)  => log.error(s"Error parsing message from server $error")
          case Right(wsMsg) =>
            log.info(s"WS [${userId.id}] <<<<  $wsMsg [$msg]")
            probe.ref ! wsMsg
        }
        Future.successful()
    }
    .to(Sink.ignore)

  val flow = Flow.fromSinkAndSource(sink, Source.fromPublisher(publisher))

  val (upgradeResponse, closed) = http.singleWebSocketRequest(req, flow)

  val connected = upgradeResponse.map { upgrade =>
    if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
      Done
    } else {
      throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
    }
  }

  def !(msg: WsMsg.In): Unit = ws ! msg

  def expectWsMsg(f: PartialFunction[Any, WsMsg.Out]): WsMsg.Out = probe.expectMsgPF(
    config.defaultTimeout, "expecting")(f orElse { case msg => {
    log.info(s"Skipping $msg")
    f(msg)
  }})

  def expectWsMsg[T: ClassTag](implicit tag: TypeTag[T]): T = probe.expectMsgPF(
    config.defaultTimeout, s"expecting type ${tag.tpe}") {
    case msg: T => msg
    case msg    =>
      log.info(s"Skipping $msg")
      expectWsMsg[T]
  }

  def closeConnection() = {
    log.info(s"Closing connection for $userId")
    ws ! Status.Success("Done")
  }

  private def decodeWsMsg(json: String): Either[Error, WsMsg.Out] = {
    Codec.Test.decodeWsMsg(json)
  }

  private def encodeWsMsg(msg: WsMsg.In): String = {
    Codec.Test.encodeWsMsg(msg)
  }
}
