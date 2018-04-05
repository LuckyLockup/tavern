package com.ll.endpoint

import cats.effect.Effect
import com.ll.utils.Logging
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.ll.domain.games.GameId
import com.ll.games.GameService
import com.ll.ws.PubSub


class SoloEndPoints [F[_] : Effect] extends Logging {
  def helloRoute(riichi: GameService)(implicit mat: Materializer): Route =
    path("riichi") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"<h1>${riichi.gamesCount} games are active!</h1>"))
      } ~
      post(
        decodeRequest {
          entity(as[String]) { id =>
            riichi.getOrCreate(GameId(id.toLong))
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Game $id is created"))
          }
        }
      )
    }

  def endpoints(pubSub: PubSub, riichi: GameService)(implicit mat: Materializer): Route =
    pathPrefix("games") {
      helloRoute(riichi)
    }
}

object SoloEndPoints {
  def endpoints[F[_] : Effect](pubSub: PubSub, riichi: GameService)(implicit mat: Materializer): Route =
    new SoloEndPoints[F].endpoints(pubSub, riichi)
}
