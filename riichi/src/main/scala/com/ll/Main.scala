package com.ll

import cats.effect.IO
import com.ll.endpoint.HelloWorld
import fs2.{Stream, StreamApp}
import fs2.StreamApp.ExitCode
import org.http4s.server.blaze._
import scala.concurrent.ExecutionContext.Implicits.global


object Main extends StreamApp[IO] {
  override def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] =
    BlazeBuilder[IO]
      .bindHttp(8080, "localhost")
      .mountService(HelloWorld.helloWorldService, "/")
      .serve
}