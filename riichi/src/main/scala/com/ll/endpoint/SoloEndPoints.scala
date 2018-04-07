package com.ll.endpoint

import akka.actor.ActorSystem
import cats.effect.Effect
import com.ll.utils.Logging
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.ll.config.ServerConfig
import com.ll.domain.auth.UserId
import com.ll.domain.games.{GameId, TableId}
import com.ll.domain.json.Codec
import com.ll.games.TablesService
import com.ll.ws.PubSub

import scala.util.{Failure, Success}


class SoloEndPoints[F[_] : Effect](config: ServerConfig)(implicit system: ActorSystem, mat: Materializer)
  extends Logging with EndpointUtils {

  implicit val timeout = config.defaultTimeout
  implicit val ec = system.dispatcher

  def helloRoute(riichi: TablesService)(implicit mat: Materializer): Route =
    path("riichi") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>${riichi.gamesCount} games are active!</h1>"))
      } ~
      post(
        decodeRequest {
          entity(as[String]) { id =>
           async(riichi.getOrCreate(TableId(id), UserId(id.toLong))
              .map(st =>
                complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, Codec.encodeWsMsg(st)))
              )
           )
          }
        }
      )
    }

  def endpoints(pubSub: PubSub, riichi: TablesService): Route =
    pathPrefix("games") {
      helloRoute(riichi)
    }
}

object SoloEndPoints {
  def endpoints[F[_] : Effect](pubSub: PubSub, riichi: TablesService, config: ServerConfig)(implicit system: ActorSystem, mat: Materializer): Route =
    new SoloEndPoints[F](config).endpoints(pubSub, riichi)
}
