package com.ll.domain.games.solo

import com.ll.domain.games.deck.{Tile, TileSet}

import scala.annotation.tailrec

sealed trait GameResult

/**
  * temporary algo for winning hand determination
  */
object GameResult {

  def handSets(hand: List[Tile]): List[List[TileSet]] = getCombinations(Nil, hand)

  def getCombinations(sets: List[List[TileSet]], remainingTiles: List[Tile]): List[List[TileSet]] = {
   remainingTiles match {
     case Nil => sets
     case elem :: Nil => Nil
     case a :: b :: Nil => TileSet.getSet(a, b) match {
       case Some(set) => addSet(set, sets)
       case None => Nil
     }
     case a :: b :: c :: tail => {
       val setsWithPairs = {
         TileSet.getSet(a, b) match {
           case Some(set) => getCombinations(addSet(set, sets), c :: tail)
           case None => Nil
         }

       }
       val setsWith3Tiles = {
         TileSet.getSet(a, b, c) match {
           case Some(set) => getCombinations(addSet(set, sets), tail)
           case None => Nil
         }
       }
       setsWith3Tiles ::: setsWithPairs
     }
   }
  }

  private def addSet(set: TileSet, list: List[List[TileSet]]): List[List[TileSet]] = {
    if (list.isEmpty) {
      List(List(set))
    } else {
      list.map(s => set :: s)
    }
  }
}
