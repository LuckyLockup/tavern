package com.ll.domain.persistence

import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.{GameType, Player, TableId}
import com.ll.domain.messages.WsMsg
import com.ll.domain.messages.WsMsg.Out.{Table, ValidationError}

trait TableState [GT <: GameType, S <: TableState[GT, S]] {
  def admin: User

  def tableId: TableId

  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (TableEvent[GT], S)]

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (TableEvent[GT], S)]

  def validateCmd(cmd: GameCmd[GT]): Either[ValidationError, List[TableEvent[GT]]]

  def applyEvent(e: TableEvent[GT]): S

  def projection(position: Option[Either[UserId, PlayerPosition[GT]]]): WsMsg.Out.TableState[GT]

  def playerIds: Set[UserId]

  def getPlayer(position: PlayerPosition[GT]): Option[Player[GT]]
}