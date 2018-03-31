package com.ll.domain.games.riichi.persistence

import com.ll.domain.games.riichi.Tile

case class PlayerState(
  closedHand: List[Tile] = Nil,
  openHand: List[Tile] = Nil,
  currentTitle: Option[Tile] = None,
  discard: List[Tile] = Nil
) {

  def isOpen = false
}