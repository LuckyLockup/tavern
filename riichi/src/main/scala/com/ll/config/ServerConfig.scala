package com.ll.config

import cats.effect.Effect
import cats.implicits._
import pureconfig.error.ConfigReaderException


case class ServerConfig(db: DatabaseConfig)

object ServerConfig {
  import pureconfig._

  def load[F[_]](implicit E: Effect[F]): F[ServerConfig] =
    E.delay(loadConfig[ServerConfig]("serverconfig")).flatMap {
      case Right(ok) => E.pure(ok)
      case Left(e) => E.raiseError(new ConfigReaderException[ServerConfig](e))
    }
}