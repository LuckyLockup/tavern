package com.ll.domain.games.riichi

import com.ll.domain.games.deck.Tile._
import org.scalatest.{FunSuite, Matchers, WordSpec}

class CommandPredictorTest extends WordSpec with Matchers {
  val tiles1 = List(
    Pin.Pin1_1,
    Pin.Pin2_1,
    Pin.Pin3_1,
    Pin.Pin4_1,

    Wan.Wan1_1,
    Wan.Wan1_2,
    Wan.Wan1_3,

    Sou.Sou3_2,
    Sou.Sou4_4,
    Sou.Sou5_1,

    Wind.East_1,
    Wind.East_2,
    Wind.East_3,
  )

  "It should compute tenpai" in {
    val res = CommandPredictor.getSets(tiles1)
    println(res)
  }

}
