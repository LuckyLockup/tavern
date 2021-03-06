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
import com.ll.domain.ws.{WsMsgCodec, WsMsgIn, WsMsgOut}
import io.circe._
import org.reactivestreams.Publisher

import scala.concurrent.Future
import scala.concurrent.duration.Duration
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
    .actorRef[WsMsgIn](16, OverflowStrategy.fail)
    .log(s"$userId sent: ")
    .map(msg => {
      val json = encodeWsMsg(msg)
      log.info(s"WS [${userId.id}] >>>> $json")
      TextMessage.Strict(json)
    })
    .toMat(Sink.asPublisher(false))(Keep.both).run()

  val sink: Sink[Message, NotUsed] = Flow[Message]
    .mapAsync(1) {
      case TextMessage.Strict(msg) =>
        log.info(s"WS [${userId.id}] <<<< $msg")
        decodeWsMsg(msg) match {
          case Left(error)  => log.error(s"Error parsing message from server $error")
          case Right(wsMsg) => probe.ref ! wsMsg
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

  def !(msg: WsMsgIn): Unit = ws ! msg

  def expectWsMsg[T<: WsMsgOut](f: PartialFunction[Any, T]): T = probe.expectMsgPF(
    config.defaultTimeout, "expecting") {
    case msg if f.isDefinedAt(msg) => f(msg)
    case msg                       =>
      log.info(s"Skipping $msg")
      expectWsMsg(f)
  }

  def expectWsMsgT[T <: WsMsgOut: ClassTag](duration: Duration = config.defaultTimeout)(implicit tag: TypeTag[T]): T = probe.expectMsgPF(
    duration, s"expecting type ${tag.tpe}") {
    case msg: T => msg
    case msg    =>
      log.info(s"Skipping $msg")
      expectWsMsgT[T](duration)
  }

  def closeConnection() = {
    log.info(s"Closing connection for $userId")
    ws ! Status.Success("Done")
  }

  private def decodeWsMsg(json: String): Either[Error, WsMsgOut] = {
    WsMsgCodec.Test.decodeWsOutMsg(json)
  }

  private def encodeWsMsg(msg: WsMsgIn): String = {
    WsMsgCodec.Test.encodeWsInMsg(msg)
  }
}
