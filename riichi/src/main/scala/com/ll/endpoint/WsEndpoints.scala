package com.ll.endpoint

import cats.effect.Effect
import org.http4s.dsl.Http4sDsl
import cats.effect._
import cats.implicits._
import com.ll.utils.Logging
import fs2._
import fs2.StreamApp.ExitCode
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.websocket._
import org.http4s.websocket.WebsocketBits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class WsEndpoints[F[_]](implicit F: Effect[F]) extends Http4sDsl[F] with Logging {

  private def createGameEndpoint(scheduler: Scheduler): HttpService[F] = HttpService[F] {
    case GET -> Root  =>
      val toClient: Stream[F, WebSocketFrame] =
        scheduler.awakeEvery[F](1.seconds).map{d =>
            log.info("Sending message")
            Text(s"Ping! $d")
        }
      val fromClient: Sink[F, WebSocketFrame] = _.evalMap { (ws: WebSocketFrame) =>
        ws match {
          case Text(t, _) => F.delay(println(t))
          case f => F.delay(println(s"Unknown type: $f"))
        }
      }
      WebSocketBuilder[F].build(toClient, fromClient)

    case GET -> Root / "wsecho" =>
      val queue = async.unboundedQueue[F, WebSocketFrame]
      val echoReply: Pipe[F, WebSocketFrame, WebSocketFrame] = _.collect {
        case Text(msg, _) => Text("You sent the server: " + msg)
        case _ => Text("Something new")
      }

      queue.flatMap { q =>
        val d = q.dequeue.through(echoReply)
        val e = q.enqueue
        WebSocketBuilder[F].build(d, e)
      }
  }

  def endpoints(scheduler: Scheduler): HttpService[F] =
    createGameEndpoint(scheduler)
  //plan another websocket connection for chat??
}

object WsEndpoints {
  def endpoints[F[_]: Effect](scheduler: Scheduler): HttpService[F] =
    new WsEndpoints[F].endpoints(scheduler)
}
