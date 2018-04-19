package com.ll.domain.games.riichi

import com.ll.domain.Const
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.Riichi.{AIPlayer, HumanPlayer}
import com.ll.domain.games.deck.Tile
import com.ll.domain.games.position.{PlayerPosition, PositionUtility}
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.{Player, TableId}
import com.ll.domain.messages.WsMsg
import com.ll.domain.messages.WsMsg.Out.Riichi.RiichiPlayerState
import com.ll.domain.messages.WsMsg.Out.{Table, ValidationError}
import com.ll.domain.persistence.RiichiGameCmd.StartGame
import com.ll.domain.persistence._

import scala.util.Random

trait RiichiTableState extends TableState[Riichi, RiichiTableState]

case class NoGameOnTable(
  admin: User,
  tableId: TableId,
  players: Set[Player[Riichi]] = Set.empty
) extends RiichiTableState {

  def validateCmd(cmd: GameCmd[Riichi]): Either[ValidationError, List[TableEvent[Riichi]]] = cmd match {
    case StartGame(`tableId`, gameId, config) =>
      if (humanPlayers.nonEmpty) {
        Right(List(RiichiEvent.GameStarted(tableId, gameId, config)))
      } else {
        Left(ValidationError("No Human players"))
      }
  }

  def applyEvent(e: TableEvent[Riichi]): RiichiTableState = e match {
    case RiichiEvent.GameStarted(_, gameId, config) =>
      def generatePlayer(position: PlayerPosition[Riichi]) = humanPlayers.find(_.position == position)
        .getOrElse(AIPlayer(config.defaultEastAi, position))

      val allTiles = Random.shuffle(Tile.allTiles)
      val (eastHand, rem1) = allTiles.splitAt(13)
      val (southHand, rem2) = rem1.splitAt(13)
      val (westHand, rem3) = rem2.splitAt(13)
      val (northHand, rem4) = rem3.splitAt(13)
      val (uraDoras, remaining) = rem4.splitAt(1)

      val east = generatePlayer(RiichiPosition.EastPosition)
      val south = generatePlayer(RiichiPosition.SouthPosition)
      val west = generatePlayer(RiichiPosition.WestPosition)
      val north = generatePlayer(RiichiPosition.NorthPosition)

      val game = GameStarted(
        admin = admin,
        tableId = tableId,
        playerStates = Map(
          east -> PlayerState(eastHand),
          south -> PlayerState(westHand),
          west -> PlayerState(westHand),
          north -> PlayerState(northHand)
        ),
        uraDoras = uraDoras,
        deck = remaining
      )
      game
  }

  def projection(position: Option[Either[UserId, PlayerPosition[Riichi]]]): WsMsg.Out.Riichi.RiichiState =
    WsMsg.Out.Riichi.RiichiState(
      tableId = tableId,
      admin = admin,
      states = humanPlayers.toList.map(p => WsMsg.Out.Riichi.RiichiPlayerState(p, Nil)),
      uraDoras = Nil,
      deck = 0
    )

  def getPlayer(position: PlayerPosition[Riichi]): Option[Player[Riichi]] = humanPlayers.find(p => p.position == position)

  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (RiichiEvent.PlayerJoined, NoGameOnTable)] = {
    if (humanPlayers.exists(_.user.id == cmd.userId)) {
      Left(ValidationError("You already joined the table"))
    } else if (players.size < 4) {
      val (newPlayers, newPlayer) = PositionUtility.addUser(players, cmd.user)
      val event = RiichiEvent.PlayerJoined(tableId, newPlayer)
      val newState = this.copy(players = newPlayers)
      Right(event, newState)
    } else {
      Left(ValidationError("Table is already full."))
    }
  }

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (RiichiEvent.PlayerLeft, NoGameOnTable)] = {
    humanPlayers.find(p => p.user.id == cmd.userId) match {
      case Some(player) =>
        val event = RiichiEvent.PlayerLeft(tableId, player)
        val newState = this.copy(players = players - player)
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
  admin: User,
  tableId: TableId,
  playerStates: Map[Player[Riichi], PlayerState],
  uraDoras: List[Tile],
  deck: List[Tile]
) extends RiichiTableState {

  def validateCmd(cmd: GameCmd[Riichi]): Either[ValidationError, List[TableEvent[Riichi]]] = ???

  def applyEvent(e: TableEvent[Riichi]): RiichiTableState = ???

  def projection(position: Option[Either[UserId, PlayerPosition[Riichi]]]): WsMsg.Out.Riichi.RiichiState =
    WsMsg.Out.Riichi.RiichiState(
      tableId = tableId,
      admin = admin,
      states = playerStates.toList.map {
        case (player, state) =>
          val hand = (player, position) match {
            case (p: HumanPlayer[Riichi], Some(Left(userId))) if p.user.id == userId =>
              state.closedHand.map(_.repr)
            case (_, Some(Right(`position`))) =>  state.closedHand.map(_.repr)
            case _ => state.closedHand.map(_ => Const.ClosedTile)
          }
          RiichiPlayerState(
            player = player,
            closedHand = hand,
            discard = state.discard.map(_.repr),
            online = state.online
          )


      },
      uraDoras = uraDoras.map(_.repr),
      deck = deck.size
    )

  def players: Set[Player[Riichi]] = playerStates.keySet

  def getPlayer(position: PlayerPosition[Riichi]): Option[Player[Riichi]] = playerStates.keySet.find(p => p.position == position)

  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (RiichiEvent.PlayerJoined, GameStarted)] = {
    ???
  }

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (RiichiEvent.PlayerLeft, GameStarted)] = ???

}