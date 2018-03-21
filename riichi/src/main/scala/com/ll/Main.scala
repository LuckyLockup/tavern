package com.ll

import cats.effect.IO
import com.ll.endpoint.{EventEndpoints, HelloWorld, WsEndpoints}
import fs2.{Scheduler, Stream, StreamApp}
import org.http4s.server.blaze._
import cats.effect._
import com.ll.config.{DatabaseConfig, ServerConfig}
import com.ll.domain.game.EventService
import com.ll.repository.doobie.DoobieEventRepositoryInterpreter
import fs2.StreamApp.ExitCode
import fs2.{Stream, StreamApp}
import org.http4s.server.blaze.BlazeBuilder


object Main extends StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def stream(args: List[String], shutdown: IO[Unit]): Stream[IO, ExitCode] =
    createStream[IO](args, shutdown)

  def createStream[F[_]](args: List[String], shutdown: F[Unit])(
    implicit E: Effect[F]): Stream[F, ExitCode] =
    for {
      conf           <- Stream.eval(ServerConfig.load[F])
      xa             <- Stream.eval(DatabaseConfig.dbTransactor(conf.db))
      _              <- Stream.eval(DatabaseConfig.initializeDb(conf.db, xa))
      scheduler      <- Scheduler[F](corePoolSize = 2)
      eventRepo      =  DoobieEventRepositoryInterpreter[F](xa)
      eventService   =  EventService[F](eventRepo)
      exitCode       <- BlazeBuilder[F]
        .bindHttp(8080, "localhost")
        .withWebSockets(true)
        .mountService(EventEndpoints.endpoints[F](eventService), "/")
        .mountService(WsEndpoints.endpoints[F](scheduler), "/ws")
        .serve
    } yield exitCode
}