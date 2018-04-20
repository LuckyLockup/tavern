package com.ll.domain.games.riichi

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player
import com.ll.domain.games.deck.Tile

case class PlayerState(
  player: Player[Riichi],
  closedHand: List[Tile],
  discard: List[Tile] = Nil,
  online: Boolean = true,
)
