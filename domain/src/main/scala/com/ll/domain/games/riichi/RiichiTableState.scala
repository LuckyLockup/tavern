package com.ll.domain.games.riichi

import com.ll.domain.ValidationError
import com.ll.domain.games.TableId
import com.ll.domain.messages.WsMsg.Out.Table
import com.ll.domain.persistence.{RiichiCmd, RiichiEvent, TableState, UserCmd}

case class RiichiTableState(
  tableId: TableId
) extends TableState[RiichiCmd, RiichiEvent] {

  def validateCmd(cmd: RiichiCmd): Either[ValidationError, (List[RiichiTableState], RiichiTableState)] = ???

  def projection(cmd: UserCmd.GetState): Table.TableState = Table.TableState(tableId)
}
