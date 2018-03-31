package com.ll.endpoint

import cats.effect.Effect
import com.ll.utils.Logging
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.ll.domain.games.GameId
import com.ll.games.riichi.Riichi
import com.ll.ws.PubSub


class RiichiEndPoints [F[_] : Effect] extends Logging {

  def helloRoute(riichi: Riichi)(implicit mat: Materializer): Route =
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

  def wsTest(pubSub: PubSub)(implicit mat: Materializer): Route =
    path("test") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, pubSub.getConnections.toString))
      }
    }

  def endpoints(pubSub: PubSub, riichi: Riichi)(implicit mat: Materializer): Route =
    pathPrefix("games") {
      helloRoute(riichi) ~ wsTest(pubSub)
    }
}

object RiichiEndPoints {
  def endpoints[F[_] : Effect](pubSub: PubSub, riichi: Riichi)(implicit mat: Materializer): Route =
    new RiichiEndPoints[F].endpoints(pubSub, riichi)
}
