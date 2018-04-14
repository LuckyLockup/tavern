package com.ll.domain.persistence

import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.Player.HumanPlayer
import com.ll.domain.games.{Player, PlayerPosition, TableId}
import com.ll.domain.messages.WsMsg.Out.{Table, ValidationError}

trait TableState [P <: PlayerPosition, C <: TableCmd, E <: GameEvent[P], S <: TableState[P,C,E,S]] {
  def adminId: User

  def tableId: TableId

  def validateCmd(cmd: C): Either[ValidationError, List[E]]

  def applyEvent(e: E): S

  def projection(cmd: UserCmd.GetState): Table.TableState

  def players: Set[Player]

  def humanPlayers: Set[HumanPlayer]

  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (UserEvent.PlayerJoined, S)]

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (UserEvent.PlayerLeft, S)]

  def startGame(cmd: TableCmd.StartGame): Either[ValidationError, (TableEvent.GameStarted, S)]

  def getPlayer(position: P): Option[Player]
}