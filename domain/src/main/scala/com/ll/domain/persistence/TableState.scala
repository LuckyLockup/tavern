package com.ll.domain.persistence

import com.ll.domain.games.Player.HumanPlayer
import com.ll.domain.games.{ Player, TableId}
import com.ll.domain.messages.WsMsg.Out.{Table, ValidationError}

trait TableState [C <: TableCmd, E <: TableEvent, S <: TableState[C,E,S]] {
  def tableId: TableId

  def validateCmd(cmd: C): Either[ValidationError, List[E]]

  def applyEvent(e: E): S

  def projection(cmd: UserCmd.GetState): Table.TableState

  def players: Set[Player]

  def humanPlayers = players.collect{case p: HumanPlayer => p}

  def humanIds = players.collect{case p: HumanPlayer => p}.map(_.userId)

  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (UserEvent.PlayerJoined, S)]

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (UserEvent.PlayerLeft, S)]
}