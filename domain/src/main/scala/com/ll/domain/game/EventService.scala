package com.ll.domain.game

import cats.Monad
import cats.data.EitherT
import com.ll.domain.ValidationError


class EventService[F[_]](repository: EventRepositoryAlgebra[F]) {

  def save(pet: Event)(implicit M: Monad[F]): EitherT[F, ValidationError, Event] = for {
    saved <- EitherT.liftF(repository.save(pet))
  } yield saved

}

object EventService {
  def apply[F[_]: Monad](repository: EventRepositoryAlgebra[F]) =
    new EventService[F](repository)
}

