package com.ll.domain.games.solo

import com.ll.domain.games.deck.Tile.Wind._
import com.ll.domain.games.deck.Tile.Sou._
import com.ll.domain.games.deck.TileSet.{Chow, Pung, TilesPair}
import org.scalatest.{FunSuite, Matchers}

class GameResultTest extends FunSuite with Matchers{
  val winningHands = Map (
    List(East_1, East_2) -> List(List(TilesPair(East_1,East_2))),
    List(Sou1_1, Sou2_1, Sou3_1) -> List(List(Chow(Sou1_1, Sou2_1, Sou3_1))),
    List(Sou1_1, Sou1_2,  Sou1_3, Sou2_1, Sou3_1, Sou4_1, Sou4_2, Sou4_3) ->
      List(
        List(TilesPair(Sou4_2,Sou4_3), Chow(Sou2_1,Sou3_1,Sou4_1), Pung(Sou1_1,Sou1_2,Sou1_3)),
        List(Pung(Sou4_1,Sou4_2,Sou4_3), Chow(Sou1_3,Sou2_1,Sou3_1), TilesPair(Sou1_1,Sou1_2))
      )
  )

  test("testHandSets") {
    winningHands.foreach { case (hand, result) =>
      GameResult.handSets(hand) should equal (result)
    }
  }
}
