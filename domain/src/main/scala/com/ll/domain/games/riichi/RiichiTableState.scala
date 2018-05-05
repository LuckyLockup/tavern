package com.ll.domain.games.riichi

import com.ll.domain.Const
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.{AIPlayer, HumanPlayer}
import com.ll.domain.games.deck.{DeclaredSet, Tile}
import com.ll.domain.games.position.{PlayerPosition, PositionUtility}
import com.ll.domain.games.riichi.result.{GameScore, HandValue, Points, TablePoints}
import com.ll.domain.games.{GameId, Player, ScheduledCommand, TableId}
import com.ll.domain.persistence._
import com.ll.domain.ops.EitherOps._
import com.ll.domain.persistence.TableCmd.RiichiCmd
import com.ll.domain.ws.WsMsgOut
import com.ll.domain.ws.WsMsgOut.ValidationError
import com.ll.domain.ws.WsRiichi.RiichiPlayerState

import scala.concurrent.duration._

sealed trait RiichiTableState extends TableState[Riichi, RiichiTableState]

case class NoGameOnTable(
  admin: User,
  tableId: TableId,
  players: Set[Player[Riichi]] = Set.empty,
  points: TablePoints = TablePoints.initialPoints
) extends RiichiTableState {

  def validateCmd(cmd: TableCmd[Riichi]): Either[WsMsgOut, List[TableEvent[Riichi]]] = cmd match {
    case TableCmd.JoinAsPlayer(_, user) =>
      players.find(p => p.senderId == user) match {
        case None   if user.isRight =>
          //TODO refactor for AI
          PositionUtility
            .addUser(players, user.right.get)
            .map(player => List(RiichiEvent.PlayerJoined(tableId, player)))
        case Some(_) =>
          Left(ValidationError("You already joined the table"))
      }

    case TableCmd.LeftAsPlayer(_, user)                =>
      players.find(p => p.senderId == user) match {
        case None         =>
          Left(ValidationError("You are not player on this table"))
        case Some(player) =>
          val newState = this.copy(players = players - player)
          Right(List(RiichiEvent.PlayerLeft(tableId, player)))
      }
    case RiichiCmd.StartGame(`tableId`, gameId, config) =>
      if (players.nonEmpty) {
        Right(List(RiichiEvent.GameStarted(tableId, gameId, config)))
      } else {
        Left(ValidationError("No Human players"))
      }
    case _                                              =>
      Left(ValidationError(s"Game is not started, ${cmd.getClass.getSimpleName} is not supported."))
  }

  def applyEvent(e: TableEvent[Riichi]): (List[ScheduledCommand[Riichi]], RiichiTableState) = e match {
    case RiichiEvent.PlayerJoined(_, player) =>
      (Nil, this.copy(players = this.players + player))

    case RiichiEvent.PlayerLeft(_, player) =>
      (Nil, this.copy(players = this.players - player))

    case RiichiEvent.GameStarted(_, gameId, config) =>
      val game: GameStarted = RiichiHelper.initializeHands(this, config, gameId)
      val nextCmd = RiichiCmd.GetTileFromWall(tableId, gameId, 1, PlayerPosition.RiichiPosition.EastPosition)
      (List(ScheduledCommand(config.nextTileDelay, nextCmd)), game)
    case _                                          => (Nil, this)
  }

  def projection(position: Option[PlayerPosition[Riichi]]): WsMsgOut.Riichi.RiichiState =
    WsMsgOut.Riichi.RiichiState(
      tableId = tableId,
      admin = admin,
      states = players.toList.map(p => RiichiPlayerState(p, Nil)),
      uraDoras = Nil,
      deck = 0,
      turn = 0,
      points = this.points
    )
}

case class GameStarted(
  admin: User,
  tableId: TableId,
  gameId: GameId,
  playerStates: List[PlayerState],
  uraDoras: List[Tile],
  deck: List[Tile],
  turn: Int = 1,
  config: RiichiConfig,
  points: TablePoints
) extends RiichiTableState {

  def validateCmd(cmd: TableCmd[Riichi]): Either[WsMsgOut, List[TableEvent[Riichi]]] = cmd match {
    case _: RiichiCmd.StartGame => Left(ValidationError("Game already started"))

    case RiichiCmd.DiscardTile(_, _, tile, commandTurn, position) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
        state <- getPlayerState(position).asEither(s"No player at $position")
        _ <- state.currentTile.nonEmpty.asEither("You can't discard tile")
        tileInHand <- (state.closedHand ::: state.currentTile.toList).find(t => t.repr == tile)
          .asEither(s"Tile $tile is not in hand")
        cmds = CommandPredictor.predictsCommandsOnDiscard(this, tileInHand, position)
      } yield List(RiichiEvent.TileDiscared(tableId, gameId, tileInHand, turn, position, cmds))

    case RiichiCmd.GetTileFromWall(_, _, commandTurn, position) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
        playerState <- getPlayerState(position).asEither("Player with this position is not found")
        tile <- this.deck.headOption.asEither("Tiles are finished")
        cmds = CommandPredictor.predictCommandsOnTileFromTheWall(this, tile, playerState)
      } yield List(RiichiEvent.TileFromTheWallTaken(tableId, gameId, tile, commandTurn, playerState.player.position, cmds))

    case RiichiCmd.DeclareTsumo(_, _, position) =>
      for {
        state <- getPlayerState(position).asEither(s"No player at $position")
        newTile <- state.currentTile.asEither("You can declare tsumo only on your tile from the wall")
        handValue <- HandValue.computeTsumoOnTile(newTile, state).asEither("Your hand is not winning.")
      } yield List(RiichiEvent.TsumoDeclared(tableId, gameId, turn, state.player.position))

    case RiichiCmd.ScoreGame(_, _)                                     =>
      val winingHand = this.playerStates.flatMap(st => HandValue.computeWin(st)).headOption
      winingHand match {
        case Some((winner, value)) =>
          val score = GameScore(
            Right(winner.player.position),
            this.playerStates
              .map {
                case `winner` => winner.player.position -> Points(value.yakus * 1000)
                case state    => state.player.position -> Points(0)
              }.toMap
          )
          val event = RiichiEvent.GameScored(tableId, gameId, turn, score)
          Right(List(event))
        case None                  => Left(ValidationError("No winniing hand"))
        //TODO add tempai
      }
    case RiichiCmd.ClaimPung(_, _, from, commandTurn, tiles, position) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
        playerState <- getPlayerState(position).asEither(s"No player at $position")
        _ <- (tiles.size == 3).asEither("Pung should contain 3 elements")
        lastTile <- this.getPlayerState(from).flatMap(st => st.discard.headOption).asEither("Discarded tile is not found")
        _ <- (lastTile.repr == tiles.head).asEither(s"Claiming pung on ${tiles.head}, but discarded tile is ${lastTile.repr}")
        pung <- playerState.pungOn(lastTile).asEither(s"Can't claim pung on ${tiles.head}")
        set = DeclaredSet(pung, from, turn)
      } yield List(RiichiEvent.TileClaimed(tableId, gameId, set, playerState.player.position))
  }

  def applyEvent(e: TableEvent[Riichi]): (List[ScheduledCommand[Riichi]], RiichiTableState) = e match {
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
      val nextCmd = RiichiCmd.GetTileFromWall(tableId, gameId, nextTurn, position.nextPosition)
      (List(ScheduledCommand(config.nextTileDelay, nextCmd)), updatedState)

    case RiichiEvent.TileFromTheWallTaken(_, _, tile, _, position, _) =>
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
    case RiichiEvent.TsumoDeclared(_, _, _, position)                 =>
      //open doras
      val scoreGame = RiichiCmd.ScoreGame(tableId, gameId)
      (List(ScheduledCommand(0 seconds, scoreGame)), this)
    case RiichiEvent.GameScored(_, _, _, score)                       =>
      (Nil, NoGameOnTable(
        this.admin,
        this.tableId,
        this.players,
        this.points.addGameScore(score))
      )
    case RiichiEvent.TileClaimed(_, _, set, position)                 =>
      (Nil, this)
  }

  def projection(position: Option[PlayerPosition[Riichi]]): WsMsgOut.Riichi.RiichiState =
    WsMsgOut.Riichi.RiichiState(
      tableId = tableId,
      admin = admin,
      states = playerStates.map {
        case state =>
          val hideTiles = (tile: Tile) => if (position.contains(state.player.position)) tile.repr else Const.ClosedTile
          RiichiPlayerState(
            player = state.player,
            closedHand = state.closedHand.map(hideTiles),
            currentTile = state.currentTile.map(hideTiles),
            discard = state.discard.map(_.repr),
            online = state.online
          )
      },
      uraDoras = uraDoras.map(_.repr),
      deck = deck.size,
      turn = turn,
      points = this.points
    )

  def players: Set[Player[Riichi]] = playerStates.map(_.player).toSet

  def getPlayerState(position: PlayerPosition[Riichi]): Option[PlayerState] = playerStates
    .find(st => st.player.position == position)
}