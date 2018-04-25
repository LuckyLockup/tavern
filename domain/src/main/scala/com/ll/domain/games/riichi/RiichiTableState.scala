package com.ll.domain.games.riichi

import com.ll.domain.Const
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.{AIPlayer, HumanPlayer}
import com.ll.domain.games.deck.Tile
import com.ll.domain.games.position.{PlayerPosition, PositionUtility}
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.{GameId, Player, ScheduledCommand, TableId}
import com.ll.domain.persistence._
import com.ll.domain.ops.EitherOps._
import com.ll.domain.ws.WsMsgIn.{GameCmd, RiichiGameCmd, UserCmd}
import com.ll.domain.ws.WsMsgOut
import com.ll.domain.ws.WsMsgOut.ValidationError
import com.ll.domain.ws.WsRiichi.RiichiPlayerState

import scala.util.Random

trait RiichiTableState extends TableState[Riichi, RiichiTableState]

case class NoGameOnTable(
  admin: User,
  tableId: TableId,
  players: Set[Player[Riichi]] = Set.empty
) extends RiichiTableState {

  def validateCmd(cmd: GameCmd[Riichi]): Either[ValidationError, List[TableEvent[Riichi]]] = cmd match {
    case RiichiGameCmd.StartGame(`tableId`, gameId, config) =>
      if (humanPlayers.nonEmpty) {
        Right(List(RiichiEvent.GameStarted(tableId, gameId, config)))
      } else {
        Left(ValidationError("No Human players"))
      }
    case _                                    =>
      Left(ValidationError(s"Game is not started, ${cmd.getClass.getSimpleName} is not supported."))
  }

  def applyEvent(e: TableEvent[Riichi]): (List[ScheduledCommand], RiichiTableState) = e match {
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
        gameId = gameId,
        playerStates = List(
          PlayerState(east, eastHand),
          PlayerState(south, southHand),
          PlayerState(west, westHand),
          PlayerState(north, northHand)
        ),
        uraDoras = uraDoras,
        deck = remaining,
        turn = 1,
        config = config
      )
      val nextCmd = RiichiGameCmd.GetTileFromWall(tableId, gameId, 1, Some(Right(RiichiPosition.EastPosition)))
      (List(ScheduledCommand(config.nextTileDelay, nextCmd)), game)
    case _                                          => (Nil, this)
  }

  def projection(position: Option[Either[UserId, PlayerPosition[Riichi]]]): WsMsgOut.Riichi.RiichiState =
    WsMsgOut.Riichi.RiichiState(
      tableId = tableId,
      admin = admin,
      states = humanPlayers.toList.map(p => RiichiPlayerState(p, Nil)),
      uraDoras = Nil,
      deck = 0,
      turn = 0
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
}

case class GameStarted(
  admin: User,
  tableId: TableId,
  gameId: GameId,
  playerStates: List[PlayerState],
  uraDoras: List[Tile],
  deck: List[Tile],
  turn: Int = 1,
  config: RiichiConfig
) extends RiichiTableState {

  def validateCmd(cmd: GameCmd[Riichi]): Either[ValidationError, List[TableEvent[Riichi]]] = cmd match {
    case _: RiichiGameCmd.StartGame => Left(ValidationError("Game already started"))

    case RiichiGameCmd.DiscardTile(_, _, tile, commandTurn, position) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
        state <- getPlayerState(position).asEither(s"No player at $position")
        _ <- state.currentTile.nonEmpty.asEither("You can't discard tile")
        tileInHand <- (state.closedHand ::: state.currentTile.toList).find(t => t.repr == tile)
          .asEither(s"Tile $tile is not in hand")
        cmds = CommandPredictor.predictsCommands(this, tileInHand, state.player.position)
      } yield List(RiichiEvent.TileDiscared(tableId, gameId, tileInHand, turn, state.player.position, cmds))

    case RiichiGameCmd.GetTileFromWall(_, _, commandTurn, position) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
        playerState <- getPlayerState(position).asEither("Player with this position is not found")
        tile <- this.deck.headOption.asEither("Tiles are finished")
      } yield List(RiichiEvent.TileFromTheWallTaken(tableId, gameId, tile, commandTurn, playerState.player.position))
  }

  def applyEvent(e: TableEvent[Riichi]): (List[ScheduledCommand], RiichiTableState) = e match {
    case RiichiEvent.TileDiscared(_, _, tile, _, position, _) =>
      val updatedStates = this.playerStates.map {
        case st if st.player.position != position => st
        case st if st.player.position == position =>
          val newClosedHand = ((st.currentTile.toList ::: st.closedHand).toSet - tile).toList
          val newDiscard = tile :: st.discard
          st.copy(
            closedHand = newClosedHand.sortBy(_.order),
            discard = newDiscard,
            currentTile = None
          )
      }
      val nextTurn = this.turn + 1
      val updatedState = this.copy(
        playerStates = updatedStates,
        turn = nextTurn
      )
      val nextCmd = RiichiGameCmd.GetTileFromWall(tableId, gameId, nextTurn, Some(Right(position.nextPosition)))
      (List(ScheduledCommand(config.nextTileDelay, nextCmd)), updatedState)

    case RiichiEvent.TileFromTheWallTaken(_, _, tile, commandTurn, position) =>
      val updatedStates = this.playerStates.map {
        case st if st.player.position != position => st
        case st if st.player.position == position => st.copy(currentTile = Some(tile))
      }
      val nextTurn = this.turn + 1
      val updatedState = this.copy(
        playerStates = updatedStates,
        turn = nextTurn,
        deck = deck.drop(1)
      )
      (List(), updatedState)
  }

  def projection(position: Option[Either[UserId, PlayerPosition[Riichi]]]): WsMsgOut.Riichi.RiichiState =
    WsMsgOut.Riichi.RiichiState(
      tableId = tableId,
      admin = admin,
      states = playerStates.map {
        case state =>
          val (hand, currentTile) = (state.player, position) match {
            case (p: HumanPlayer[Riichi], Some(Left(userId))) if p.user.id == userId =>
              (state.closedHand.map(_.repr), state.currentTile.map(_.repr))
            case (_, Some(Right(`position`)))                                        =>
              (state.closedHand.map(_.repr), state.currentTile.map(_.repr))
            case _                                                                   =>
              (state.closedHand.map(_ => Const.ClosedTile), state.currentTile.map(_ => Const.ClosedTile))
          }

          RiichiPlayerState(
            player = state.player,
            closedHand = hand,
            currentTile = currentTile,
            discard = state.discard.map(_.repr),
            online = state.online
          )
      },
      uraDoras = uraDoras.map(_.repr),
      deck = deck.size,
      turn = turn
    )

  def players: Set[Player[Riichi]] = playerStates.map(_.player).toSet

  def getPlayer(position: PlayerPosition[Riichi]): Option[Player[Riichi]] = playerStates
    .map(_.player)
    .find(p => p.position == position)

  def getPlayerState(position: Option[Either[UserId, PlayerPosition[Riichi]]]): Option[PlayerState] = position match {
    case Some(Left(userId)) => this.playerStates.find {
      case st => st.player match {
        case p: HumanPlayer[Riichi] => p.user.id == userId
        case _                      => false
      }
    }
    case Some(Right(pos))   => this.playerStates.find(st => st.player.position == pos)
    case None               => None
  }

  def joinGame(cmd: UserCmd.JoinAsPlayer): Either[ValidationError, (RiichiEvent.PlayerJoined, GameStarted)] = {
    ???
  }

  def leftGame(cmd: UserCmd.LeftAsPlayer): Either[ValidationError, (RiichiEvent.PlayerLeft, GameStarted)] = ???

}