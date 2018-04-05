package com.ll

import akka.{Done, NotUsed}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.WebSocketRequest
import akka.actor.{ActorRef, ActorSystem, Status}
import akka.http.scaladsl.model.StatusCodes
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Source}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import akka.stream.scaladsl.Sink
import akka.testkit.{TestKitBase, TestProbe}
import com.ll.domain.ws.WsMsg
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import org.reactivestreams.Publisher

import scala.concurrent.Future
import scala.concurrent.duration._


class WsConnection(userId: Int, as: ActorSystem, mat: Materializer)
  extends TestKitBase with Logging {

  implicit lazy val system = as
  implicit lazy val materializer = mat

  log.info(s"Creating connection for $userId")
  implicit val ec = system.dispatcher

  val req = WebSocketRequest(uri = s"ws://127.0.0.1:8080/api/v0.1/ws/$userId")

  val probe = TestProbe()

  val (ws: ActorRef, publisher: Publisher[TextMessage.Strict]) = Source
    .actorRef[WsMsg.In](16, OverflowStrategy.fail)
    .log(s"$userId sent: ")
    .map(msg =>  TextMessage.Strict(encodeWsMsg(msg)))
    .toMat(Sink.asPublisher(false))(Keep.both).run()

  val sink: Sink[Message, NotUsed] = Flow[Message]
      .log(s"$userId received: ")
    .mapAsync(1) {
      case TextMessage.Strict(msg)  =>
        decodeWsMsg(msg) match {
          case Left(error) => log.error(s"Error parsing message from server $error")
          case Right(wsMsg) => probe.ref ! wsMsg
        }
        Future.successful()
    }
    .to(Sink.ignore)

  val flow = Flow.fromSinkAndSource(sink, Source.fromPublisher(publisher))

  val (upgradeResponse, closed) = Http().singleWebSocketRequest(req, flow)

  val connected = upgradeResponse.map { upgrade =>
    if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
      Done
    } else {
      throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
    }
  }

  def !(msg: WsMsg.In): Unit = ws ! msg

  def expect(f: PartialFunction[Any, WsMsg.Out]): WsMsg.Out = probe.expectMsgPF(3 seconds, "expecting")(f)

  private def decodeWsMsg(json: String): Either[Error, WsMsg.Out] = {
    decode[WsMsg.Out](json)
  }

  private def encodeWsMsg(msg: WsMsg.In): String = {
    msg.asJson.noSpaces
  }
}
