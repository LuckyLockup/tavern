package com.ll.domain.games.riichi

import com.ll.domain.games.deck.Tile.Pin._
import com.ll.domain.games.deck.TileSet.Chow
import org.scalatest.{FunSuite, Matchers, WordSpec}

class SetsDistrubutionTest extends WordSpec with Matchers {
  val examples = Map(
    List(
      Pin1_1,
      Pin2_1,
      Pin3_1,
      Pin4_1
    ) -> List(
      SetsDistrubution(
        List(Chow(Pin1_1, Pin2_1, Pin3_1)),
        List(Pin4_1),
        Pin4_1),
      SetsDistrubution(
        List(Chow(Pin2_1, Pin3_1, Pin4_1)),
        List(Pin1_1),
        Pin1_1)
    ),

  )

  "Should generate sets from tiles" in {

  }

}
