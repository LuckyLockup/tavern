package com.ll.domain.games.riichi

import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.Riichi.HumanPlayer
import com.ll.domain.games.position.{PlayerPosition, PositionUtility}
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.{Player, TableId}
import com.ll.domain.messages.WsMsg
import com.ll.domain.messages.WsMsg.Out.{Table, ValidationError}
import com.ll.domain.persistence._

trait RiichiTableState extends TableState[Riichi, RiichiTableState]

case class NoGameOnTable(
  adminId: User,
  tableId: TableId,
  humanPlayers: Set[HumanPlayer] = Set.empty
) extends RiichiTableState {

  def playerIds = humanPlayers.map(_.user.id)

  def validateCmd(cmd: GameCmd[Riichi]): Either[ValidationError, List[GameEvent[Riichi]]] = cmd match {
    case _ => ???
  }

  def applyEvent(e: GameEvent[Riichi]): RiichiTableState = ???

  def projection(cmd: UserCmd.GetState): WsMsg.Out.Riichi.RiichiState = WsMsg.Out.Riichi.RiichiState(
    tableId,
    humanPlayers.toList
  )

  def players: Set[Player[Riichi]] = humanPlayers.toSet

  def getPlayer(position: PlayerPosition[Riichi]): Option[Player[Riichi]] = humanPlayers.find(p => p.position == position)

  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (RiichiEvent.PlayerJoined, NoGameOnTable)] = {
    if (humanPlayers.exists(_.user.id == cmd.userId)) {
      Left(ValidationError("You already joined the table"))
    } else if (players.size < 4) {
      val (newPlayers, newPlayer) = PositionUtility.addUser(humanPlayers, cmd.user)
      val event = RiichiEvent.PlayerJoined(tableId, newPlayer)
      val newState = this.copy(humanPlayers = newPlayers)
      Right(event, newState)
    } else {
      Left(ValidationError("Table is already full."))
    }
  }

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (RiichiEvent.PlayerLeft, NoGameOnTable)] = {
    humanPlayers.find(p => p.user.id == cmd.userId) match {
      case Some(player) =>
        val event = RiichiEvent.PlayerLeft(tableId, player)
        val newState = this.copy(humanPlayers = humanPlayers - player)
        Right(event, newState)
      case None         =>
        Left(ValidationError("You are not player on this table"))
    }
  }
  //
  //  def startGame(cmd: TableCmd.StartGame): Either[ValidationError, (TableEvent.GameStarted, RiichiTableState)] = {
  //    if (players.isEmpty) {
  //      Left(ValidationError("You can't start game without players."))
  //    } else {
  //      //TODO put instead of not existing players Ai.
  //      ???
  //    }
  //  }

}

case class GameStarted(
  adminId: User,
  tableId: TableId,
  hands: Map[Player[Riichi], PlayerState]
) extends RiichiTableState {

  def playerIds = hands.keySet.collect{case p: HumanPlayer => p.user.id}

  def validateCmd(cmd: GameCmd[Riichi]): Either[ValidationError, List[GameEvent[Riichi]]] = ???

  def applyEvent(e: GameEvent[Riichi]): RiichiTableState = ???

  def projection(cmd: UserCmd.GetState): WsMsg.Out.Riichi.RiichiState = ???

  def players: Set[Player[Riichi]] = hands.keySet

  def getPlayer(position: PlayerPosition[Riichi]): Option[Player[Riichi]] = hands.keySet.find(p => p.position == position)

  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (RiichiEvent.PlayerJoined, GameStarted)] = ???

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (RiichiEvent.PlayerLeft, GameStarted)] = ???


}