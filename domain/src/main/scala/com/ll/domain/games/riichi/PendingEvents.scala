package com.ll.domain.games.riichi

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.persistence.{RiichiEvent, TableEvent}

case class PendingEvents(
  nextTile: TableEvent[Riichi],
  rons: List[RiichiEvent.RonDeclared] = Nil,
  pung: Option[RiichiEvent.PungClaimed] = None,
  chow: Option[RiichiEvent.ChowClaimed] = None,
)
