package com.ll.domain.persistence

import com.ll.domain.games.TableId
import com.ll.domain.messages.WsMsg.Out.Table

trait TableState [C <: TableCmd, E <: TableEvent] {
  def tableId: TableId

  //There are 2 types of commands. s
  def receiveCmd(cmd: TableCmd) = ???

  def projection(cmd: UserCmd.GetState): Table.TableState
}