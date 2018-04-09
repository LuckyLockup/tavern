package com.ll.domain.games.riichi

import com.ll.domain.ValidationError
import com.ll.domain.games.{Player, TableId}
import com.ll.domain.messages.WsMsg.Out.Table
import com.ll.domain.persistence._

trait RiichiTableState extends TableState[RiichiCmd, RiichiEvent, RiichiTableState] {
  def validateCmd(cmd: RiichiCmd): Either[ValidationError, List[RiichiEvent]] = ???

  def applyEvent(e: RiichiEvent): RiichiTableState = ???

  def projection(cmd: UserCmd.GetState): Table.TableState = Table.TableState(tableId)

  def players: Set[Player]
}

case class NoGame(
  tableId: TableId,
  players: Set[Player] = Set.empty[Player]
) extends RiichiTableState {
}

case class GameStarted(
  tableId: TableId,
  hands: Map[Player, PlayerState]
) extends RiichiTableState {

  def players = hands.keySet
}