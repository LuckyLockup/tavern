package com.ll.domain.games.riichi

import com.ll.domain.games.deck.{Tile, TileCode, TileSet}

object TileSetsHelper {
  case class GroupedTiles(
    sets: List[TileSet] = Nil,
    waitingTiles: List[Tile] = Nil)

  def groupTiles(tiles: List[Tile], grouped: List[GroupedTiles]): List[GroupedTiles] = tiles match {
    case Nil => grouped

  }

  /**
    * If by adding additional tile to the tile list set can be combined,
    * then additional tiles are returned. Otherwise Nil is returned
    *
    * @param tiles
    * @return if returns None, it means tiles can't construct TileSet.
    */
  def tilesWaitFor(tiles: List[Tile]): List[String] = tiles.sortBy(_.order) match {
    case t :: Nil                                  => List(t.repr)
    case t1 :: t2 :: Nil if TileSet.isPair(t1, t2) => List(t1.repr)
    case (t1: Tile.Pin) :: (t2: Tile.Pin) :: Nil =>
      generateCodesForNumbers(t1.number, t2.number, TileCode.generatePinCode)
    case (t1: Tile.Wan) :: (t2: Tile.Wan) :: Nil =>
      generateCodesForNumbers(t1.number, t2.number, TileCode.generateWanCode)
    case (t1: Tile.Sou) :: (t2: Tile.Sou) :: Nil =>
      generateCodesForNumbers(t1.number, t2.number, TileCode.generateSouCode)
    case _                                       => Nil
  }

  def generateCodesForNumbers(number1: Int, number2: Int, gen: Int => String): List[String] = (number1, number2) match {
    case (1, 2)                   => List(gen(3))
    case (8, 9)                   => List(gen(7))
    case (n1, n2) if n1 + 1 == n2 => List(gen(n1 - 1), gen(n2 + 1))
    case (n1, n2) if n1 + 2 == n2 => List(gen(n1 + 1))
    case _                        => Nil
  }
}
