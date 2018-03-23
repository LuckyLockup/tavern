package com.ll.endpoint

import cats.effect.Effect
import com.ll.utils.Logging
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.javadsl.Flow

class HelloWorldEndpoints[F[_]: Effect] extends Logging {

  def helloRoute: Route =
    path("hello") {
      get {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Here be dragons!</h1>"))
      }
    }

  def endpoints(): Route =
    helloRoute
}

object HelloWorldEndpoints {
  def endpoints[F[_]: Effect](): Route =
    new HelloWorldEndpoints[F].endpoints()
}


