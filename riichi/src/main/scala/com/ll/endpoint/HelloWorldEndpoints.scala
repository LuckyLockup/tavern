package com.ll.endpoint

import akka.NotUsed
import cats.effect.Effect
import com.ll.utils.Logging
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage, UpgradeToWebSocket}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteResult}
import akka.stream.Materializer
import com.ll.domain.auth.UserId
import com.ll.infra.PubSub

class HelloWorldEndpoints[F[_]: Effect] extends Logging {

  def helloRoute: Route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Here be dragons!</h1>"))
      }
    }

  def greeterWebSocketService(implicit mat: Materializer): Flow[Message, TextMessage, NotUsed] =
    Flow[Message].mapConcat {
      case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream) :: Nil
      case bm: BinaryMessage =>
        bm.dataStream.runWith(Sink.ignore)
        Nil
    }

  def wsRoute(pubSub: PubSub[F])(implicit mat: Materializer) = path("ws" / LongNumber) { id =>
    get { ctx =>
      ctx.request match {
        case req @ HttpRequest(HttpMethods.GET, Uri.Path("/ws"), _, _, _) =>
          req.header[UpgradeToWebSocket] match {
            case Some(upgrade) =>
              log.info("Creating ws connection")
//              handleWebSocketMessages(pubSub.openConnection(UserId(id)))
              val conn = upgrade.handleMessages(pubSub.openConnection(UserId(id)))
              ctx.complete(conn)
            case None          =>
              ctx.complete(HttpResponse(400, entity = "Not a valid websocket request!"))
          }
        case r: HttpRequest =>
          r.discardEntityBytes() // important to drain incoming HTTP Entity stream
          ctx.complete(HttpResponse(404, entity = "Unknown resource!"))
      }
    }
  }

  def endpoints(pubSub: PubSub[F])(implicit mat: Materializer): Route =
    helloRoute ~ wsRoute(pubSub)
}

object HelloWorldEndpoints {
  def endpoints[F[_]: Effect](pubSub: PubSub[F])(implicit mat: Materializer): Route =
    new HelloWorldEndpoints[F].endpoints(pubSub)
}


