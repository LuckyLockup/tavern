package com.ll.utils

import com.ll.domain.auth.UserId
import com.ll.domain.games.GameId

import scala.util.Random

class CommonData {
  val userId = UserId(Random.nextInt(1000))
  val gameId = GameId(Random.nextInt(1000) + 100000)
}
