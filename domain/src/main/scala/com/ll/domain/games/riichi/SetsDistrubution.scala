package com.ll.domain.games.riichi

import com.ll.domain.games.deck.{Tile, TileSet}

case class SetsDistrubution(
  sets: List[TileSet],
  waiting: List[Tile],
  waitOn: Tile
)

object SetsDistrubution {
  def apply(tiles: List[Tile]): List[SetsDistrubution] = {
    ???
  }
}
