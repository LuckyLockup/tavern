package com.ll.domain.games.riichi

import org.scalatest.Matchers

class DeckTest extends org.scalatest.FunSuite with Matchers {

  test("Number of tiles") {
    //136
    Deck.allTiles.size equals 4 * (
      3 //winds
      + 4 //dragons
      + 3 * 9 //simples
    )

    Deck.allTiles.map(_.order).sorted should contain theSameElementsInOrderAs (1 to 136)
  }

}
