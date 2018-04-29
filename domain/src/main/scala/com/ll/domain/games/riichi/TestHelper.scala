package com.ll.domain.games.riichi

import com.ll.domain.games.deck.Tile

import scala.annotation.tailrec
import scala.util.Random

object TestHelper {

  def prepareTiles(tiles: List[String]): List[Tile] = {
    _prepareTiles(tiles, Random.shuffle(Tile.allTiles), Nil)
  }

  @tailrec
  def _prepareTiles(tiles: List[String], rem: List[Tile], sorted: List[Tile]): List[Tile] = tiles match {
    case Nil => sorted ::: rem
    case head :: tail =>
      val tileOpt = rem.find(t => t.repr == head)
      tileOpt match {
        case None => sorted ::: rem
        case Some(tile) => _prepareTiles(tail, rem.filter(t => t != tile), sorted ::: List(tile))
      }
  }
}
