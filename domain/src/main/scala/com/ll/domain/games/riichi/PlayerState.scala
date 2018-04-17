package com.ll.domain.games.riichi

import com.ll.domain.games.deck.Tile

case class PlayerState(
  closedHand: List[Tile],
  discard: List[Tile] = Nil,
  online: Boolean = true,
)
