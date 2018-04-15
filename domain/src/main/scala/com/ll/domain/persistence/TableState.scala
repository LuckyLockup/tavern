package com.ll.domain.persistence

import com.ll.domain.auth.User
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.{GameType, Player, TableId}
import com.ll.domain.messages.WsMsg
import com.ll.domain.messages.WsMsg.Out.{Table, ValidationError}

trait TableState [GT <: GameType, S <: TableState[GT, S]] {
  def adminId: User

  def tableId: TableId

  def validateCmd(cmd: GameCmd[GT]): Either[ValidationError, List[GameEvent[GT]]]

  def applyEvent(e: GameEvent[GT]): S

  def projection(cmd: UserCmd.GetState): WsMsg.Out.TableState[GT]

  def players: Set[Player[GT]]

  def getPlayer(position: PlayerPosition[GT]): Option[Player[GT]]
}