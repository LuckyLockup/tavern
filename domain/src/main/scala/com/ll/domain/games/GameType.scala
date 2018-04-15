package com.ll.domain.games

sealed trait GameType

object GameType {
  sealed trait Riichi extends GameType
}
