package com.ll.domain.games.solo.persistence

import com.ll.domain.games.deck.Tile

case class PlayerState(
  closedHand: List[Tile] = Nil,
  openHand: List[Tile] = Nil,
  currentTitle: Option[Tile] = None,
  discard: List[Tile] = Nil
) {

  def isOpen = false
}