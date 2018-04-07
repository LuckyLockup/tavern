package com.ll.domain.games.riichi

import com.ll.domain.games.{GameTable, TableId}
import com.ll.domain.messages.WsMsg.Out.Table
import com.ll.domain.persistence.{RiichiCmd, RiichiEvent, UserCmd}

case class RiichiTable(
  tableId: TableId

) extends GameTable[RiichiCmd, RiichiEvent, RiichiGameState]{
  var _game: Option[RiichiGameState] = None

  def projection(cmd: UserCmd.GetState): Table.TableState = Table.TableState(tableId)
}
