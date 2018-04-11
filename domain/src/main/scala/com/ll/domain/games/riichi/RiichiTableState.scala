package com.ll.domain.games.riichi

import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.Player.HumanPlayer
import com.ll.domain.games.{Player, TableId}
import com.ll.domain.messages.WsMsg.Out.{Table, ValidationError}
import com.ll.domain.persistence._

trait RiichiTableState extends TableState[RiichiCmd, RiichiEvent, RiichiTableState] {
  def validateCmd(cmd: RiichiCmd): Either[ValidationError, List[RiichiEvent]] = ???

  def applyEvent(e: RiichiEvent): RiichiTableState = ???

  def projection(cmd: UserCmd.GetState): Table.TableState = Table.TableState(tableId, players)

}

case class NoGameOnTable(
  adminId: User,
  tableId: TableId,
  players: Set[Player] = Set.empty[Player]
) extends RiichiTableState {
  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (UserEvent.PlayerJoined, RiichiTableState)] = {
    if (players.size < 4) {
      val newPlayer = HumanPlayer(cmd.user)
      val event = UserEvent.PlayerJoined(tableId, newPlayer)
      val newState = this.copy(players = this.players + newPlayer)
      Right(event, newState)
    } else {
      Left(ValidationError("Table is already full."))
    }
  }

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (UserEvent.PlayerLeft, RiichiTableState)] = {
    players.collect{case p: HumanPlayer => p }.find(p => p.userId == cmd.userId) match {
      case Some(player) =>
        val event = UserEvent.PlayerLeft(tableId, player)
        val newState = this.copy(players = this.players - player)
        Right(event, newState)
      case None =>
        Left(ValidationError("You are not player on this table"))
    }
  }

  def startGame(cmd: TableCmd.StartGame): Either[ValidationError, (UserEvent.PlayerLeft, RiichiTableState)] = {
    if (players.isEmpty) {
      Left(ValidationError("You can't start game without players."))
    } else {
      //TODO put instead of not existing players Ai.
      ???
    }
  }
}

case class GameStarted(
  adminId: User,
  tableId: TableId,
  hands: Map[Player, PlayerState]
) extends RiichiTableState {

  def players = hands.keySet

  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (UserEvent.PlayerJoined, RiichiTableState)] = {
    //TODO implement
    Left(ValidationError("Logic is not implemented"))
  }

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (UserEvent.PlayerLeft, RiichiTableState)] = {
    //TODO implement
    Left(ValidationError("Logic is not implemented"))
  }

  def startGame(cmd: TableCmd.StartGame): Either[ValidationError, (UserEvent.PlayerLeft, RiichiTableState)] = {
    Left(ValidationError("Game is already started"))
  }
}