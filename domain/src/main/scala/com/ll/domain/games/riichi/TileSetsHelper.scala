package com.ll.domain.games.riichi

import com.ll.domain.games.deck.{Tile, TileCode, TileSet}

import scala.annotation.tailrec
import scala.collection.immutable
import scala.reflect.ClassTag

object TileSetsHelper {
  case class GroupedTiles(
    sets: List[TileSet] = Nil,
    waitingTiles: List[Tile] = Nil)

  def groupTiles(tiles: List[Tile], grouped: List[GroupedTiles]): List[GroupedTiles] = tiles match {
    case Nil => grouped

  }
  /**
    * Compute tenpai for ordered hand
    */
  def tenpai(tiles: List[Tile]): List[SetsCombination] = {
    _tenpai(tiles, Nil, Nil).filter(comb =>
      comb.notInSets.nonEmpty
    )
  }

  case class SetsCombination(sets: List[TileSet], notInSets: List[Tile]) {
    def waitingOn: List[String] = tilesWaitFor(notInSets)
  }

  private def _tenpai(
    tiles: List[Tile],
    notInSets: List[Tile],
    alreadyFoundSets: List[List[TileSet]]
  ): List[SetsCombination] = {
    tiles match {
      case Nil => alreadyFoundSets.map(sets => SetsCombination(sets, notInSets))

      case head :: tail =>
        val putToNotInSets = tilesWaitFor(head :: notInSets) match {
          case Nil => Nil
          case _   => _tenpai(tail, (head :: notInSets).sortBy(_.order), alreadyFoundSets)
        }
        val combineSets = findSetsForTiles(head, tail) match {
          case Nil => Nil
          case combinations =>
            combinations.map {
            case (set, remaining) =>
              println(s"$head : Found set $set, remaining $remaining, notInSets $notInSets, already: $alreadyFoundSets")
              val newSets = alreadyFoundSets match {
                case Nil  => List(List(set))
                case sets => sets.map(s => set :: s)
              }
              _tenpai(remaining, notInSets, newSets)
          }
        }
        putToNotInSets ::: combineSets
    }
}

def findSetsForTiles (tile: Tile, tiles: List[Tile] ): List[(TileSet, List[Tile] )] = {
  val (same, notSame) = tiles.span (thisTile => {
  (tile, thisTile) match {
  case (t1: Tile.Sou, t2: Tile.Sou) if t1.number == t2.number => true
  case (t1: Tile.Wan, t2: Tile.Wan) if t1.number == t2.number => true
  case (t1: Tile.Pin, t2: Tile.Pin) if t1.number == t2.number => true
  case (_: Tile.East, _: Tile.East) => true
  case (_: Tile.South, _: Tile.South) => true
  case (_: Tile.West, _: Tile.West) => true
  case (_: Tile.North, _: Tile.North) => true
  case (_: Tile.Red, _: Tile.Red) => true
  case (_: Tile.Green, _: Tile.Green) => true
  case (_: Tile.White, _: Tile.White) => true
  case _ => false
}
})
  val pairs = same match {
  case t1 :: others => List ((TileSet.TilesPair (tile, t1), others ::: notSame) )
  case Nil => Nil
}
  val pungs = same match {
  case t1 :: t2 :: others => List ((TileSet.Pung (tile, t1, t2), others ::: notSame) )
  case _ => Nil
}

  val chows = tile match {
  case givenTile: Tile.Sou => findChow[Tile.Sou] (givenTile, tiles)
  case givenTile: Tile.Wan => findChow[Tile.Wan] (givenTile, tiles)
  case givenTile: Tile.Pin => findChow[Tile.Pin] (givenTile, tiles)
  case _ => Nil
}

  pairs ::: pungs ::: chows
}

  private def findChow[T <: Tile.Number: ClassTag] (tile: T, tiles: List[Tile] ): List[(TileSet.Chow, List[Tile] )] = {
  val nextTile = tiles.collectFirst {
  case t: T if tile.number + 1 == t.number => t
}
  val afterNextTile = tiles.collectFirst {
  case t: T if tile.number + 2 == t.number => t
}
  (nextTile, afterNextTile) match {
  case (Some (t1), Some (t2) ) =>
  val i1 = tiles.indexOf (t1)
  val rem1 = tiles.patch (i1, Nil, 1)
  val i2 = rem1.indexOf (t2)
  val remaining = rem1.patch (i2, Nil, 1)
  List ((TileSet.Chow (tile, t1, t2), remaining) )
  case _ => Nil
}
}

  /**
    * If by adding additional tile to the tile list set can be combined,
    * then additional tiles are returned. Otherwise Nil is returned
    *
    * @param tiles
    * @return if returns None, it means tiles can't construct TileSet.
    */
  def tilesWaitFor (tiles: List[Tile] ): List[String] = tiles.sortBy (_.order) match {
  case t :: Nil => List (t.repr)
  case t1 :: t2 :: Nil if TileSet.isPair (t1, t2) => List (t1.repr)
  case (t1: Tile.Pin) :: (t2: Tile.Pin) :: Nil =>
  generateCodesForNumbers (t1.number, t2.number, TileCode.generatePinCode)
  case (t1: Tile.Wan) :: (t2: Tile.Wan) :: Nil =>
  generateCodesForNumbers (t1.number, t2.number, TileCode.generateWanCode)
  case (t1: Tile.Sou) :: (t2: Tile.Sou) :: Nil =>
  generateCodesForNumbers (t1.number, t2.number, TileCode.generateSouCode)
  case _ => Nil
}

  private def generateCodesForNumbers (
  number1: Int,
  number2: Int,
  gen: Int => String): List[String] = (number1, number2) match {
  case (1, 2) => List (gen (3) )
  case (8, 9) => List (gen (7) )
  case (n1, n2) if n1 + 1 == n2 => List (gen (n1 - 1), gen (n2 + 1) )
  case (n1, n2) if n1 + 2 == n2 => List (gen (n1 + 1) )
  case _ => Nil
}
}
