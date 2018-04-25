package com.ll.domain.games.riichi

import com.ll.domain.TileHelper
import org.scalatest.{Matchers, WordSpec}

import scala.io.Source

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
      ("1_pin", List("2_pin", "3_pin", "4_pin")) -> Map("chow" -> List("4_pin")),
      ("2_pin", List("2_pin", "2_pin", "4_pin")) -> Map("pair" -> List("2_pin", "4_pin"), "pung" -> List("4_pin")),
      ("2_pin", List("2_pin", "3_pin", "4_pin")) -> Map("pair" -> List("3_pin", "4_pin"), "chow" -> List("2_pin")),
      ("east", List()) -> Map.empty[String, List[String]],
      ("east", List("east", "west")) -> Map("pair" -> List("west")),
      ("west", List("west", "west")) -> Map("pair" -> List("west"), "pung" -> Nil)
    )
    testData.map {
      case ((tile, tiles), result) => ((tile.riichiTile, tiles.map(_.riichiTile)), result)
    }.foreach {
      case ((tile, tiles), expected) =>
        val resultList = TileSetsHelper.findSetsForTiles(tile, tiles).map {
          case (set, rem) =>
            (set.toString, rem.map(_.repr))
        }
        val resultMap = resultList.toMap
        resultList.size should be(resultMap.size)
        resultList.size should be(expected.size)
        resultList.foreach {
          case (actualSet, actualRemaining) =>
            expected.get(actualSet) should not be empty
            expected.get(actualSet).get should contain theSameElementsAs actualRemaining
        }
    }
  }

  "Tenpai" in {
    val testData = Map(
      List("1_pin", "2_pin", "3_pin", "1_wan", "2_wan", "3_wan", "east", "east", "east", "red")
        -> List("red"),
      List("1_pin", "1_pin", "1_wan", "2_wan", "3_wan", "east", "east", "east", "2_sou", "3_sou")
        -> List("1_sou", "4_sou"),
      List("1_pin", "1_pin", "1_wan", "2_wan", "3_wan", "east", "east", "east", "2_sou", "4_sou")
        -> List("3_sou"),
      List("1_pin", "1_pin", "1_wan", "2_wan", "3_wan", "east", "east", "east", "2_sou", "5_sou")
        -> List(),
      List("3_pin", "3_pin", "3_pin",
           "4_pin", "4_pin", "4_pin",
           "5_pin", "6_pin", "7_pin",
           "8_pin", "8_pin", "8_pin", "9_pin") -> List("4_pin", "7_pin", "8_pin", "9_pin")
    )
    testData.map {
      case (tiles, expectedTiles) => (tiles.map(_.riichiTile), expectedTiles)
    }.foreach {
      case (tiles, expectedTiles) =>
        val combs = TileSetsHelper.tenpai(tiles)
        combs.foreach(println)

        TileSetsHelper.tenpai(tiles).flatMap(_.waitingOn).distinct should contain theSameElementsAs expectedTiles
    }
  }

  "Tenpai for prepared data" in {
    import io.circe._, io.circe.generic.semiauto._, io.circe.parser.decode
    case class TestData(tiles: List[String], waiting: List[String])
    implicit val fooDecoder: Decoder[TestData] = deriveDecoder[TestData]
    val resource = Source.fromResource("tenpai_data.json").getLines.mkString
    println(resource)
    val testData = decode[List[TestData]](resource).getOrElse(throw new Exception("Not correct data"))
    testData.foreach{
      case TestData(tiles, waiting) =>
        val combs = TileSetsHelper.tenpai(tiles.map(_.riichiTile))
        println(s"$tiles -> $combs")
        combs.flatMap(_.waitingOn).distinct should contain theSameElementsAs waiting
    }
    println(testData)
  }
}
