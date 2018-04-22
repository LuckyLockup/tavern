package com.ll.domain.persistence

import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.Player.{AIPlayer, HumanPlayer}
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.{GameType, Player, ScheduledCommand, TableId}
import com.ll.domain.ws.WsMsgIn.{GameCmd, UserCmd}
import com.ll.domain.ws.WsMsgOut
import com.ll.domain.ws.WsMsgOut.ValidationError

trait TableState[GT <: GameType, S <: TableState[GT, S]] {
  def admin: User

  def tableId: TableId

  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (TableEvent[GT], S)]

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (TableEvent[GT], S)]

  def validateCmd(cmd: GameCmd[GT]): Either[ValidationError, List[TableEvent[GT]]]

  def applyEvent(e: TableEvent[GT]): (List[ScheduledCommand], S)

  def projection(position: Option[Either[UserId, PlayerPosition[GT]]]): WsMsgOut.TableState[GT]

  def players: Set[Player[GT]]

  def humanPlayers: Set[HumanPlayer[GT]] = players.collect { case p: HumanPlayer[GT] => p }

  def aiPlayers: Set[AIPlayer[GT]] = players.collect { case p: AIPlayer[GT] => p }

  def playerIds: Set[UserId] = humanPlayers.map(_.user.id)

  def getPlayer(position: PlayerPosition[GT]): Option[Player[GT]]
}