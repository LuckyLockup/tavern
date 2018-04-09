package com.ll.utils

import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.{GameId, TableId}

import scala.util.Random

class CommonData {
  val tableId = TableId("table_" + Random.nextInt(1000))
  val userId = UserId(Random.nextInt(1000))
  val user = User(userId, "Ozu")
  val gameId = GameId(Random.nextInt(1000) + 100000)
}
