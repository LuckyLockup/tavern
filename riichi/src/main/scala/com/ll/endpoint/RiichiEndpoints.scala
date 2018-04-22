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
import com.ll.domain.messages.HttpMessage.Riichi.CreateTable
import com.ll.domain.ws.WsMsgCodec
import com.ll.games.TablesService
import com.ll.ws.PubSub
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.util.{Failure, Success}

class RiichiEndpoints[F[_] : Effect](config: ServerConfig)(implicit system: ActorSystem, mat: Materializer)
  extends Logging with EndpointUtils {
  import WsMsgCodec._

  implicit val timeout = config.defaultTimeout
  implicit val ec = system.dispatcher

  def endpoints(pubSub: PubSub, riichi: TablesService): Route =
    path("games" / "riichi") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>${riichi.gamesCount} games are active!</h1>"))
      } ~
        post(
          decodeRequest {
            entity(as[CreateTable]) { req =>
              riichi.getOrCreate(req.tableId, req.userId)
              complete(HttpEntity(ContentTypes.`application/json`, "Table creation is started"))
            }
          }
        )
    }
}

object RiichiEndpoints {
  def endpoints[F[_] : Effect](pubSub: PubSub, riichi: TablesService, config: ServerConfig)(implicit system: ActorSystem, mat: Materializer): Route =
    new RiichiEndpoints[F](config).endpoints(pubSub, riichi)
}
