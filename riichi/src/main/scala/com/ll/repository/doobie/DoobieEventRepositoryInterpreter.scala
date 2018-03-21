package com.ll.repository.doobie

import cats.Monad
import com.ll.domain.game.{Event, EventRepositoryAlgebra}
import doobie.Transactor
import cats._
import cats.data.NonEmptyList
import cats.implicits._

class DoobieEventRepositoryInterpreter[F[_]: Monad](val xa: Transactor[F])
  extends EventRepositoryAlgebra[F] {

  def save(event: Event): F[Event] = {
    event.pure[F]
  }
}

object DoobieEventRepositoryInterpreter {
  def apply[F[_]: Monad](xa: Transactor[F]): DoobieEventRepositoryInterpreter[F] =
    new DoobieEventRepositoryInterpreter(xa)
}
