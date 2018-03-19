package com.ll.domain.game

trait EventRepositoryAlgebra[F[_]] {
  def save(event: Event): F[Event]
}
