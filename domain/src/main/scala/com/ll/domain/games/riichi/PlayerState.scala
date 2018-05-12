package com.ll.domain.games.riichi

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player
import com.ll.domain.games.deck.{DeclaredSet, DiscardedTile, Tile, TileSet}
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.riichi.result.HandValue

case class PlayerState(
  player: Player[Riichi],
  closedHand: List[Tile],
  currentTile: Option[Tile] = None,
  openHand: List[DeclaredSet[Riichi]] = Nil,
  discard: List[DiscardedTile[Riichi]] = Nil,
  online: Boolean = true,
) {
  def pungOn(tile: Tile): Option[TileSet.Pung] = {
    TileSetsHelper.findSetsForTiles(tile, closedHand)
      .map(_._1)
      .collectFirst { case p@TileSet.Pung(_, _, _) => p }
  }

  def chowsOn(tile: Tile): List[TileSet.Chow] = {
    TileSetsHelper.findSetsForTiles(tile, closedHand)
      .map(_._1)
      .collect { case p@TileSet.Chow(_, _, _) => p }
  }

  def shouldDiscardTile(turn: Int): Boolean = currentTile.nonEmpty ||
    openHand.headOption.map(_.turn).contains(turn - 1)

  def ronOn(tile: Tile): Option[HandValue] = {
    TileSetsHelper
      .tenpai(closedHand)
      .filter(combination => combination.waitingOn.contains(tile.repr))
      .map(_ => HandValue(1, 1))
      .headOption
  }

  def tsumoOn(tile: String): Option[HandValue] = {
    TileSetsHelper
      .tenpai(closedHand)
      .filter(combination => combination.waitingOn.contains(tile))
      .map(_ => HandValue(1, 1))
      .headOption
  }
}
