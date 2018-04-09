package com.ll.domain.games.riichi

import com.ll.domain.games.{HumanPlayer, Player, TableId}
import com.ll.domain.messages.WsMsg.Out.{Table, ValidationError}
import com.ll.domain.persistence._

trait RiichiTableState extends TableState[RiichiCmd, RiichiEvent, RiichiTableState] {
  def validateCmd(cmd: RiichiCmd): Either[ValidationError, List[RiichiEvent]] = ???

  def applyEvent(e: RiichiEvent): RiichiTableState = ???

  def projection(cmd: UserCmd.GetState): Table.TableState = Table.TableState(tableId, players)

}

case class NoGameOnTable(
  tableId: TableId,
  players: Set[Player] = Set.empty[Player]
) extends RiichiTableState {
  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (UserEvent.PlayerJoined, NoGameOnTable)] = {
    if (players.size < 4) {
      val event = UserEvent.PlayerJoined(tableId, cmd.player)
      val newState = this.copy(players = this.players + cmd.player)
      Right(event, newState)
    } else {
      Left(ValidationError("Table is already full."))
    }
  }

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (UserEvent.PlayerLeft, NoGameOnTable)] = {
    players.collect{case p: HumanPlayer => p }.find(p => p.userId == cmd.userId) match {
      case Some(p) =>
        val event = UserEvent.PlayerLeft(tableId, cmd.player)
        val newState = this.copy(players = this.players - cmd.player)
        Right(event, newState)
      case None =>
        Left(ValidationError("You are not player on this table"))
    }
  }
}

case class GameStarted(
  tableId: TableId,
  hands: Map[Player, PlayerState]
) extends RiichiTableState {

  def players = hands.keySet

  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (UserEvent.PlayerJoined, GameStarted)] = ???

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (UserEvent.PlayerLeft, GameStarted)] = ???
}