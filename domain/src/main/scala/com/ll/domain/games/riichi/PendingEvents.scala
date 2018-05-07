package com.ll.domain.games.riichi

import com.ll.domain.persistence.RiichiEvent
import com.ll.domain.persistence.RiichiEvent.TileFromTheWallTaken

case class PendingEvents(
  nextTile: TileFromTheWallTaken,
  rons: List[RiichiEvent.RonDeclared] = Nil,
  pung: Option[RiichiEvent.PungClaimed] = None,
  chow: Option[RiichiEvent.ChowClaimed] = None,
)
