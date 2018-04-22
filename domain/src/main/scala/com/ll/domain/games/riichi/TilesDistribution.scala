package com.ll.domain.games.riichi

import com.ll.domain.games.deck.Tile
import com.ll.domain.games.deck.Tile._

case class TilesDistribution(
  pins: List[Pin],
  sous: List[Sou],
  wans: List[Wan],

  easts: List[East],
  souths: List[South],
  wests: List[West],
  norths: List[North],

  whites: List[White],
  greens: List[Green],
  reds: List[Red]
)

object TilesDistribution {
  def apply(tiles: List[Tile]): TilesDistribution = TilesDistribution(
    pins = tiles.collect { case p: Tile.Pin => p }.sortBy(_.order),
    sous = tiles.collect { case p: Tile.Sou => p }.sortBy(_.order),
    wans = tiles.collect { case p: Tile.Wan => p }.sortBy(_.order),

    easts = tiles.collect { case p: Tile.East => p },
    souths = tiles.collect { case p: Tile.South => p },
    wests = tiles.collect { case p: Tile.West => p },
    norths = tiles.collect { case p: Tile.North => p },

    whites = tiles.collect { case p: Tile.White => p },
    greens = tiles.collect { case p: Tile.Green => p },
    reds = tiles.collect { case p: Tile.Red => p }
  )
}
