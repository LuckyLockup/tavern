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

class RiichiEndpoints[F[_] : Effect](config: ServerConfig)(implicit system: ActorSystem, mat: Materializer)
  extends Logging with EndpointUtils {

  implicit val timeout = config.defaultTimeout
  implicit val ec = system.dispatcher

  def endpoints(pubSub: PubSub, riichi: TablesService): Route =
    path("games" / "riichi") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>${riichi.gamesCount} games are active!</h1>"))
      } ~
        post(
          decodeRequest {
            entity(as[String]) { id =>
              async(riichi.getOrCreate(TableId(id), UserId(0))
                .map(st => {
                  log.info(s"returning: ${Codec.encodeWsMsg(st)}")
                  complete(HttpEntity(ContentTypes.`application/json`, Codec.encodeWsMsg(st)))
                })
              )
            }
          }
        )
    }
}

object RiichiEndpoints {
  def endpoints[F[_] : Effect](pubSub: PubSub, riichi: TablesService, config: ServerConfig)(implicit system: ActorSystem, mat: Materializer): Route =
    new RiichiEndpoints[F](config).endpoints(pubSub, riichi)
}
