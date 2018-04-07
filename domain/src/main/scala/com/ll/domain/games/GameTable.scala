package com.ll.domain.games

import com.ll.domain.ValidationError
import com.ll.domain.auth.UserId
import com.ll.domain.messages.WsMsg.Out.Table
import com.ll.domain.persistence.{GameState, TableCmd, TableEvent, UserCmd}

/*

 */
abstract class GameTable[C <: TableCmd, E <: TableEvent, S <: GameState] {
  def tableId: TableId
  protected def _game: Option[S]

  //There are 2 types of commands. s
  def receiveCmd(cmd: TableCmd) = ???

  def projection(cmd: UserCmd.GetState): Table.TableState
}
