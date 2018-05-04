package com.ll.domain.persistence

import com.ll.domain.ai.ServiceId
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.{GameType, Player, ScheduledCommand, TableId}
import com.ll.domain.ws.WsMsgIn.{GameCmd, JoinLeftCmd, PlayerCmd}
import com.ll.domain.ws.WsMsgOut
import com.ll.domain.ws.WsMsgOut.ValidationError

trait TableState[GT <: GameType, S <: TableState[GT, S]] {
  import com.ll.domain.ops.EitherOps._

  def admin: User

  def tableId: TableId

  def joinLeftCmd(cmd: JoinLeftCmd, user: Either[ServiceId, User]): Either[ValidationError, (WsMsgOut, S)]

  def playerCmd(cmd: PlayerCmd[GT], position: PlayerPosition[GT]): Either[ValidationError, List[TableEvent[GT]]]

  def gameCmd(cmd: GameCmd[GT]): Either[ValidationError, List[TableEvent[GT]]]

  def applyEvent(e: TableEvent[GT]): (List[ScheduledCommand[GT]], S)

  def projection(position: Option[PlayerPosition[GT]]): WsMsgOut.TableState[GT]

  def players: Set[Player[GT]]

  def getPlayer(position: PlayerPosition[GT]): Either[ValidationError, Player[GT]] =
    players.find(p => p.position == position).asEither(s"No player at $position")

  def getPosition(senderId: Either[ServiceId, UserId]): Either[ValidationError, PlayerPosition[GT]] = players
    .find(p => p.senderId == senderId)
    .map(p => p.position)
    .asEither(s"No player with $senderId")
}