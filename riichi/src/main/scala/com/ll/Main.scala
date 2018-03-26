package com.ll

import com.ll.config.{DatabaseConfig, ServerConfig}
import com.ll.domain.game.EventService
import com.ll.repository.doobie.DoobieEventRepositoryInterpreter
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.ll.endpoint.HelloWorldEndpoints
import cats.effect.IO
import cats.effect._
import com.ll.ws.PubSub
import com.ll.utils.Logging

import scala.io.StdIn


object Main extends App with Logging {

  override def main(args: Array[String]): Unit = {
    bootstrap(args.toList)
  }

  def bootstrap(args: List[String])(implicit E: Effect[IO]) = {
    val program: IO[Unit] = for {
      conf <- ServerConfig.load[IO]
      xa             <- DatabaseConfig.dbTransactor(conf.db)
      _              <- DatabaseConfig.initializeDb(conf.db, xa)
      system = ActorSystem("ll")
      materializer = ActorMaterializer()(system)
      eventRepo      =  DoobieEventRepositoryInterpreter[IO](xa)
      eventService   =  EventService[IO](eventRepo)
      pubSub = PubSub[IO](system, materializer)
      exitCode       <- IO {
        log.info("Starting server...")
        implicit val sys = system
        implicit val mat = materializer
        implicit val executionContext = system.dispatcher
        val route = HelloWorldEndpoints.endpoints[IO](pubSub)

        Http().bindAndHandle(route, "localhost", 8080)
      }
      _ <- IO {
        try {
          println("Press ENTER to exit the system")
          StdIn.readLine()
        } finally {
          system.terminate()
        }
      }
    } yield ()
    program.unsafeRunSync()

  }


}