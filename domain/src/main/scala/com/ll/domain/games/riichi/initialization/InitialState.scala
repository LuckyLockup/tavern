package com.ll.domain.games.riichi.initialization

import com.ll.domain.games.deck.Tile
import com.ll.domain.games.riichi.TestingState

import scala.util.Random

case class InitialState(
  eastHand: List[Tile] = Nil,
  southHand: List[Tile] = Nil,
  westHand: List[Tile] = Nil,
  northHand: List[Tile] = Nil,
  uraDoras: List[Tile] = Nil,
  deadWall: List[Tile] = Nil,
  wall: List[Tile] = Nil
)

object InitialState {
  def apply(st: TestingState): InitialState = {
    val tiles = Random.shuffle(Tile.allTiles)
    val (eastHand, rem1) = findTiles(st.eastHand, tiles)
    val (southHand, rem2) = findTiles(st.southHand, rem1)
    val (westHand, rem3) = findTiles(st.westHand, rem2)
    val (northHand, rem4) = findTiles(st.northHand, rem3)

    val (uraDoras, rem5) = findTiles(st.uraDoras, rem4)
    val (deadWall, rem6) = findTiles(st.deadWall, rem5)
    val (wall, rem7) = findTiles(st.wall, rem6)

    val (eastHandAll, rem8) = fillTiles(eastHand, 14, rem7)
    val (southHandAll, rem9) = fillTiles(southHand, 13, rem8)
    val (westHandAll, rem10) = fillTiles(westHand, 13, rem9)
    val (northHandAll, rem11) = fillTiles(northHand, 13, rem10)

    val (uraDorasAll, rem12) = fillTiles(uraDoras, 1, rem10)
    val (deadWallAll, rem13) = fillTiles(deadWall, 9, rem10)
    val wallAll = wall ::: rem13
    InitialState(
      eastHand = eastHandAll,
      southHand = southHandAll,
      westHand = westHandAll,
      northHand = northHandAll,
      uraDoras = uraDorasAll,
      deadWall = deadWallAll,
      wall = wallAll
    )
  }

  private def findTiles(names: List[String], remainingTiles: List[Tile], converted: List[Tile] = Nil): (List[Tile], List[Tile]) =
    names match {
      case Nil => (converted.reverse, remainingTiles)
      case head :: tail =>
        val foundTile = remainingTiles.find(t => t.repr == head).get
        val remaining = remainingTiles.filter(t => t != foundTile)
        findTiles(tail, remaining, foundTile :: converted)
    }

  private def fillTiles(tiles: List[Tile], size: Int, remaining: List[Tile]): (List[Tile], List[Tile]) = {
    val (addition, rem) = remaining.splitAt(size - tiles.size)
    (tiles ::: addition, rem)
  }
}
