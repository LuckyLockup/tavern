package com.ll.domain.persistence

import com.ll.domain.ValidationError
import com.ll.domain.games.TableId
import com.ll.domain.messages.WsMsg.Out.Table

trait TableState [C <: TableCmd, E <: TableEvent] {
  def tableId: TableId

//  def validateCmd[S <: TableState[C, E]](cmd: C): Either[ValidationError, (List[E], S)]

  def projection(cmd: UserCmd.GetState): Table.TableState
}