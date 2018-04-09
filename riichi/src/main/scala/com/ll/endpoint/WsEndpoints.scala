package com.ll.endpoint

import cats.effect.Effect
import com.ll.utils.Logging
import akka.http.scaladsl.model.ws.UpgradeToWebSocket
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.ll.domain.auth.UserId
import com.ll.domain.messages.WsMsg
import com.ll.games.TablesService
import com.ll.ws.PubSub

class WsEndpoints[F[_] : Effect] extends Logging {

  def helloRoute: Route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Here be dragons!</h1>"))
      }
    }

  def wsTest(pubSub: PubSub): Route =
    path("test") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, pubSub.getConnections.toString))
      }
    } ~
    path("ws" / LongNumber) { id =>
      post {
        decodeRequest {
          entity(as[String]) { str =>
            pubSub.sendToUser(UserId(id), WsMsg.Out.Message(str))
            complete(HttpResponse(200, entity = "We are ok"))
          }
        }
      }
    }

  def wsRoute(pubSub: PubSub, riichi: TablesService)(implicit mat: Materializer) = path("ws" / LongNumber) { id =>
    get { ctx =>
      ctx.request match {
        case req@HttpRequest(HttpMethods.GET, _, _, _, _) =>
          req.header[UpgradeToWebSocket] match {
            case Some(upgrade) =>
              ctx.complete(upgrade.handleMessages(pubSub.openNewConnection(UserId(id), riichi)))
            case None          =>
              ctx.complete(HttpResponse(400, entity = "Not a valid websocket request!"))
          }
        case r: HttpRequest                               =>
          r.discardEntityBytes() // important to drain incoming HTTP Entity stream
          ctx.complete(HttpResponse(404, entity = "Unknown resource!"))
      }
    }
  }

  def endpoints(pubSub: PubSub, riichi: TablesService)(implicit mat: Materializer): Route =
    helloRoute ~ wsRoute(pubSub, riichi) ~ wsTest(pubSub)
}

object WsEndpoints {
  def endpoints[F[_] : Effect](pubSub: PubSub, riichi: TablesService)(implicit mat: Materializer): Route =
    new WsEndpoints[F].endpoints(pubSub, riichi: TablesService)
}


