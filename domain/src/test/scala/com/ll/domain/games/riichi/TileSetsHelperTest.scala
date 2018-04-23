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

  "Find sets for tiles" in {
    val testData = Map(
      ("1_pin",  List("2_pin", "3_pin", "4_pin")) -> Map("chow" -> List("4_pin")),
      ("2_pin",  List("2_pin", "2_pin", "4_pin")) -> Map("pair" -> List("2_pin", "4_pin"), "pung" -> List("4_pin")),
      ("2_pin",  List("2_pin", "3_pin", "4_pin")) -> Map("pair" -> List("3_pin", "4_pin"), "chow" -> List("2_pin")),
      ("east",  List()) -> Map.empty[String, List[String]],
      ("east",  List("east", "west")) ->  Map("pair" -> List("west")),
      ("west",  List("west", "west")) ->  Map("pair" -> List("west"), "pung" -> Nil)
    )
    testData.map {
      case ((tile, tiles), result) => ((tile.riichiTile, tiles.map(_.riichiTile)), result)
    }.foreach {
      case ((tile, tiles), expected) =>
        val resultList = TileSetsHelper.findSetsForTiles(tile, tiles).map{
          case (set, rem) =>
            (set.short, rem.map(_.repr))
        }
        val resultMap = resultList.toMap
        resultList.size should be (resultMap.size)
        resultList.size should be (expected.size)
        resultList.foreach{
          case (actualSet, actualRemaining) =>
            expected.get(actualSet) should not be empty
            expected.get(actualSet).get should contain theSameElementsAs actualRemaining
        }

    }
  }
}
