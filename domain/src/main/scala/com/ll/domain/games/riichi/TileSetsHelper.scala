package com.ll.domain.games.riichi

import com.ll.domain.games.deck.{Tile, TileCode, TileSet}

import scala.annotation.tailrec

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
   _tenpai(tiles, Nil)
  }

  case class SetsCombination(sets: List[TileSet], notInSets: List[Tile]) {
    def waitingOn: List[String] = tilesWaitFor(notInSets)
  }

  private def _tenpai(tiles: List[Tile], notInSets: List[Tile]): List[SetsCombination] = {
    tiles match {
      case Nil          => List(SetsCombination(Nil, notInSets))
      case head :: tail => findSetsForTiles(head, tail) match {
        case Nil => if (tilesWaitFor(head :: notInSets).nonEmpty) {
          _tenpai(tail, head :: notInSets)
        } else {
          Nil
        }
        case combinations => combinations.flatMap{
          case (set, remaining) =>
            _tenpai(remaining, notInSets).map{
            case comb => comb.copy(sets = set :: comb.sets)
          }
        }
      }
    }
  }

  def findSetsForTiles(tile: Tile, tiles: List[Tile]): List[(TileSet, List[Tile])] = {
    val (same, notSame) = tiles.span(thisTile => {
      (tile, thisTile) match {
        case (t1: Tile.Sou, t2: Tile.Sou) if t1.number == t2.number => true
        case (t1: Tile.Wan, t2: Tile.Wan) if t1.number == t2.number => true
        case (t1: Tile.Pin, t2: Tile.Pin) if t1.number == t2.number => true
        case (_: Tile.East, _: Tile.East)                           => true
        case (_: Tile.South, _: Tile.South)                         => true
        case (_: Tile.West, _: Tile.West)                           => true
        case (_: Tile.North, _: Tile.North)                         => true
        case (_: Tile.Red, _: Tile.Red)                             => true
        case (_: Tile.Green, _: Tile.Green)                         => true
        case (_: Tile.White, _: Tile.White)                         => true
        case _                                                      => false
      }
    })
    val pairs = same match {
      case t1 :: others => List((TileSet.TilesPair(tile, t1), others ::: notSame))
      case Nil          => Nil
    }
    val pungs = same match {
      case t1 :: t2 :: others => List((TileSet.Pung(tile, t1, t2), others ::: notSame))
      case _                  => Nil
    }
    val nextTile = tiles.find(t => {
      (tile, t) match {
        case (t1: Tile.Sou, t2: Tile.Sou) if t1.number + 1 == t2.number => true
        case (t1: Tile.Wan, t2: Tile.Wan) if t1.number + 1 == t2.number => true
        case (t1: Tile.Pin, t2: Tile.Pin) if t1.number + 1 == t2.number => true
        case _                                                          => false
      }
    })
    val afterNextTile = tiles.find(t => {
      (tile, t) match {
        case (t1: Tile.Sou, t2: Tile.Sou) if t1.number + 2 == t2.number => true
        case (t1: Tile.Wan, t2: Tile.Wan) if t1.number + 2 == t2.number => true
        case (t1: Tile.Pin, t2: Tile.Pin) if t1.number + 2 == t2.number => true
        case _                                                          => false
      }
    })
    val chows = (nextTile, afterNextTile) match {
      case (Some(t1), Some(t2)) =>
        val remaining = tiles.filter(t => t != t1 && t != t2)
        List((TileSet.Chow(tile, t1, t2), remaining))
      case _                    => Nil
    }
    pairs ::: pungs ::: chows
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
    case (t1: Tile.Pin) :: (t2: Tile.Pin) :: Nil   =>
      generateCodesForNumbers(t1.number, t2.number, TileCode.generatePinCode)
    case (t1: Tile.Wan) :: (t2: Tile.Wan) :: Nil   =>
      generateCodesForNumbers(t1.number, t2.number, TileCode.generateWanCode)
    case (t1: Tile.Sou) :: (t2: Tile.Sou) :: Nil   =>
      generateCodesForNumbers(t1.number, t2.number, TileCode.generateSouCode)
    case _                                         => Nil
  }

  private def generateCodesForNumbers(
    number1: Int,
    number2: Int,
    gen: Int => String): List[String] = (number1, number2) match {
    case (1, 2)                   => List(gen(3))
    case (8, 9)                   => List(gen(7))
    case (n1, n2) if n1 + 1 == n2 => List(gen(n1 - 1), gen(n2 + 1))
    case (n1, n2) if n1 + 2 == n2 => List(gen(n1 + 1))
    case _                        => Nil
  }
}
