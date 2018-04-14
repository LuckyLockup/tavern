package com.ll.domain.games.riichi

import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.Player.HumanPlayer
import com.ll.domain.games.{Player, TableId}
import com.ll.domain.messages.WsMsg.Out.{Table, ValidationError}
import com.ll.domain.persistence._

trait RiichiTableState extends TableState[RiichiPosition, RiichiCmd, RiichiEvent, RiichiTableState] {
  def validateCmd(cmd: RiichiCmd): Either[ValidationError, List[RiichiEvent]] = ???

  def applyEvent(e: RiichiEvent): RiichiTableState = ???

  def projection(cmd: UserCmd.GetState): Table.TableState = Table.TableState(tableId, players)

}

case class NoGameOnTable(
  adminId: User,
  tableId: TableId,
  humanPlayers: Set[HumanPlayer] = Set.empty
) extends RiichiTableState {
  def players: Set[Player] = humanPlayers.toSet

  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (UserEvent.PlayerJoined, RiichiTableState)] = {
    if (humanPlayers.exists(_.user.id == cmd.userId)) {
      Left(ValidationError("You already joined the table"))
    } else if (players.size < 4) {
      val (newPlayers, newPlayer) = RiichiPosition.addUser(humanPlayers, cmd.user)
      val event = UserEvent.PlayerJoined(tableId, newPlayer)
      val newState = this.copy(humanPlayers = newPlayers)
      Right(event, newState)
    } else {
      Left(ValidationError("Table is already full."))
    }
  }

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (UserEvent.PlayerLeft, RiichiTableState)] = {
    humanPlayers.find(p => p.user.id == cmd.userId) match {
      case Some(player) =>
        val event = UserEvent.PlayerLeft(tableId, player)
        val newState = this.copy(humanPlayers = humanPlayers - player)
        Right(event, newState)
      case None =>
        Left(ValidationError("You are not player on this table"))
    }
  }

  def startGame(cmd: TableCmd.StartGame): Either[ValidationError, (TableEvent.GameStarted, RiichiTableState)] = {
    if (players.isEmpty) {
      Left(ValidationError("You can't start game without players."))
    } else {
      //TODO put instead of not existing players Ai.
      ???
    }
  }

  def getPlayer(position: RiichiPosition): Option[Player] = humanPlayers.find(p => p.position == position)
}

case class GameStarted(
  adminId: User,
  tableId: TableId,
  hands: Map[Player, PlayerState]
) extends RiichiTableState {

  def players = hands.keySet

  def humanPlayers: Set[HumanPlayer] = ???

  def getPlayer(position: RiichiPosition): Option[Player] = hands.keySet.find(p => p.position == position)

  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (UserEvent.PlayerJoined, RiichiTableState)] = {
    //TODO implement
    Left(ValidationError("Logic is not implemented"))
  }

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (UserEvent.PlayerLeft, RiichiTableState)] = {
    //TODO implement
    Left(ValidationError("Logic is not implemented"))
  }

  def startGame(cmd: TableCmd.StartGame): Either[ValidationError, (TableEvent.GameStarted, RiichiTableState)] = {
    Left(ValidationError("Game is already started"))
  }
}