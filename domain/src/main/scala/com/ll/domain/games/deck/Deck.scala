package com.ll.domain.games.deck

object Deck {
  val riichiTiles = Tile.allTiles

  val tilesForSolo = Tile.allTiles.filter {
    case _ : Tile.Dragon => true
    case _ : Tile.Sou => true
    case _ => false
  }
}
