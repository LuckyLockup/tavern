package com.ll.domain.games.riichi

import com.ll.domain.games.deck.Tile
import org.scalatest.Matchers

class TileTest extends org.scalatest.FunSuite with Matchers {

  test("Number of tiles") {
    //136
    Tile.allTiles.size equals 4 * (
      3 //winds
      + 4 //dragons
      + 3 * 9 //simples
    )

    Tile.allTiles.map(_.order).sorted should contain theSameElementsInOrderAs (1 to 136)
  }

}
