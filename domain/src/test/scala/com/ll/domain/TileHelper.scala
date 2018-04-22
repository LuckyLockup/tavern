package com.ll.domain

import com.ll.domain.games.deck.{Tile}

trait TileHelper {
  implicit class RiichiRepr(str: String) {
    def riichiTile: Tile = Tile.allTiles.find(t => t.repr == str).get
  }
}
