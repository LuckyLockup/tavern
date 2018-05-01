package com.ll.domain.games.riichi

import com.ll.domain.games.deck.{Tile, TileCode, TileSet}

object TileSetsHelper {
  case class GroupedTiles(
    sets: List[TileSet] = Nil,
    waitingTiles: List[Tile] = Nil)

  /**
    * Compute tenpai for ordered hand
    */
  def tenpai(tiles: List[Tile]): List[SetsCombination] = {
    _tenpai(SetsCombination(tiles, Nil, Nil))
  }

  case class SetsCombination(
    remainingTiles: List[Tile],
    sets: List[TileSet],
    notInSets: List[Tile]
  ) {
    def waitingOn: List[String] = tilesWaitFor(notInSets)

    override def toString: String = s"sets: ${sets.map(_.toString).sorted.mkString("|")}, waiting: ${notInSets.map(_.repr).sorted.mkString(",")}"
  }

  private def _tenpai(
    combination: SetsCombination
  ): List[SetsCombination] = {
    combination match {
      case SetsCombination(Nil, _, _)          => List(combination)
      case SetsCombination(head :: tail, sets, notInSets) =>
        val combinationsWithoutHeadInSets = tilesWaitFor(head :: notInSets) match {
          case Nil => Nil
          case _   =>
            _tenpai(SetsCombination(tail, sets, head :: notInSets))
        }
        val combinationsWithHeadInSets = findSetsForTiles(head, tail) match {
          case Nil => Nil
          case combinations => combinations.flatMap{
            case (set, remaining) =>
              val newSets = set :: sets
              val pairs = newSets.collect{case t: TileSet.TilesPair => t}.size
              if (pairs > 1 && newSets.size > pairs){
                Nil
              } else {
                _tenpai(SetsCombination(remaining, set :: sets, notInSets))
              }
          }
        }
        (combinationsWithHeadInSets ::: combinationsWithoutHeadInSets).groupBy(_.toString)
          .map(_._2.head).toList
    }
  }

  def findSetsForTiles(tile: Tile, unsortedTiles: List[Tile]): List[(TileSet, List[Tile])] = {
    val tiles = unsortedTiles.sortBy(_.order)

    val same = tiles.collect{
      case t if TileSet.isPair(tile, t) => t
    }
    val notSame = tiles.collect{
      case t if !TileSet.isPair(tile, t) => t
    }

    val pairs = same match {
      case t1 :: others => List((TileSet.TilesPair(tile, t1), others ::: notSame))
      case Nil          => Nil
    }
    val pungs = same match {
      case t1 :: t2 :: others => List((TileSet.Pung(tile, t1, t2), others ::: notSame))
      case _                  => Nil
    }

    val chows = tile match {
      case givenTile: Tile.Number => findChow(givenTile, tiles)
      case _                   => Nil
    }
    pairs ::: pungs ::: chows
  }

  private def findChow(tile: Tile.Number, tiles: List[Tile]): List[(TileSet.Chow, List[Tile])] = {
    val nextTile = tiles.collectFirst {
      case t: Tile.Number if tile.number + 1 == t.number && tile.suit == t.suit=> t
    }
    val afterNextTile = tiles.collectFirst {
      case t: Tile.Number if tile.number + 2 == t.number && tile.suit == t.suit => t
    }
    (nextTile, afterNextTile) match {
      case (Some(t1), Some(t2)) if TileSet.isChow(tile, t1, t2) =>
        val i1 = tiles.indexOf(t1)
        val rem1 = tiles.patch(i1, Nil, 1)
        val i2 = rem1.indexOf(t2)
        val remaining = rem1.patch(i2, Nil, 1)
        List((TileSet.Chow(tile, t1, t2), remaining))
      case _                    => Nil
    }
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
