package com.ll.domain.games.riichi

import org.scalatest.{FunSuite, Matchers}

class TileSetTest extends FunSuite with Matchers {

  test("test Pung") {
    val pungs = List(
      (Tile.Wind.West_1, Tile.Wind.West_2, Tile.Wind.West_3)
    )
    pungs.foreach { case (x, y, z) =>
      TileSet.getSet(x, y, z) should equal (Some(TileSet.Pung(x, y, z)))
    }

  }

  test("test non pungs") {
    val pungs = List(
      (Tile.Wind.West_1, Tile.Wind.West_2, Tile.Wind.East_1)
    )
    pungs.foreach { case (x, y, z) =>
      TileSet.getSet(x, y, z) should equal (None)
    }

  }

}
