package com.ll.domain.games.riichi

import com.ll.domain.TileHelper
import org.scalatest.{Matchers, WordSpec}

class TileSetsHelperTest extends WordSpec with Matchers with TileHelper {

  "Waiting tiles" in {
    val testData = Map(
      List("west") -> List("west"),
      List("1_pin", "1_pin") -> List("1_pin"),
      List("west", "east") -> Nil,
      List("2_pin", "3_pin") -> List("1_pin", "4_pin"),
      List("8_sou", "9_sou") -> List("7_sou"),
      List("7_wan", "9_wan") -> List("8_wan"),
      List("7_wan", "7_wan", "7_wan") -> Nil
    )

    testData
      .map {
        case (k, v) => (k.map(_.riichiTile), v)
      }
      .foreach {
        case (tiles, expected) =>
          TileSetsHelper.tilesWaitFor(tiles) should be(expected)
      }
  }
}
