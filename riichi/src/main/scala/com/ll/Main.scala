package com.ll

import com.ll.config.{DatabaseConfig, ServerConfig}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.ll.endpoint.{SoloEndPoints, WsEndpoints}
import cats.effect.IO
import cats.effect._
import com.ll.games.GameService
import com.ll.ws.PubSub
import com.ll.utils.Logging



object Main extends App with Logging {

  override def main(args: Array[String]): Unit = {
    bootstrap(args.toList)
  }

  def bootstrap(args: List[String])(implicit E: Effect[IO]) = {
    val program: IO[Unit] = for {
      conf <- ServerConfig.load[IO]
      xa             <- DatabaseConfig.dbTransactor(conf.db)
      _              <- DatabaseConfig.initializeDb(conf.db, xa)
      system         = ActorSystem("ll")
      strategy       = decider()
      materializer   = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(strategy))(system)
      pubSub         = PubSub[IO](system, materializer)
      gameService    = GameService[IO](system, pubSub)
      _              <- IO {
        log.info("Starting server...")
        implicit val sys = system
        implicit val mat = materializer
        implicit val executionContext = system.dispatcher
        import akka.http.scaladsl.server.Directives._
        val route: Route = pathPrefix("api" / "v0.1") {
           WsEndpoints.endpoints[IO](pubSub, gameService) ~
           SoloEndPoints.endpoints[IO](pubSub, gameService)
        }

        Http().bindAndHandle(route, "0.0.0.0", 8080)
        log.info("Server is started...")
      }
    } yield ()
    program.unsafeRunSync()
  }

  def decider(): Supervision.Decider = {
    case ex =>
      log.error("Error in stream: " + ex.getMessage)
      Supervision.Resume
    case _                      =>
      Supervision.Stop
  }
}