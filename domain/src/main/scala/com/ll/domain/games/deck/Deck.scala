package com.ll.domain.games.deck

object Deck {
  val tilesForSolo = Tile.allTiles.filter {
    case _ : Tile.Dragon => true
    case _ : Tile.Sou => true
    case _ => false
  }
}
