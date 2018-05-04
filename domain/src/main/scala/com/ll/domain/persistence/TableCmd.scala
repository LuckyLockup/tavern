package com.ll.domain.persistence

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.{GameType, TableId}

sealed trait TableCmd[GT <: GameType] {
  def tableId: TableId
}

sealed trait RiichiCmd extends TableCmd[Riichi]

object RiichiCmd {

}