package com.ll.domain.games.riichi

import com.ll.domain.Const
import com.ll.domain.ai.ServiceId
import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.{AIPlayer, HumanPlayer}
import com.ll.domain.games.deck.{DeclaredSet, Tile}
import com.ll.domain.games.position.{PlayerPosition, PositionUtility}
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.riichi.result.{GameScore, HandValue, Points, TablePoints}
import com.ll.domain.games.{GameId, Player, ScheduledCommand, TableId}
import com.ll.domain.persistence._
import com.ll.domain.ops.EitherOps._
import com.ll.domain.ws.WsMsgIn.{PlayerCmd, RiichiWsCmd, JoinLeftCmd}
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

  def tableCmd(cmd: JoinLeftCmd, user: User): Either[ValidationError, (WsMsgOut, NoGameOnTable)] = cmd match {
    case JoinLeftCmd.JoinAsPlayer(_) =>
      players.collectFirst{ case p @ HumanPlayer(`user`, _) => p} match {
        case None if players.size < 4 =>
          val (newPlayers, newPlayer) = PositionUtility.addUser(players, user)
          val newState = this.copy(players = newPlayers)
          Right(WsMsgOut.Riichi.PlayerJoinedTable(tableId, newPlayer), newState)
        case None =>
          Left(ValidationError("Table is already full"))
        case Some(_) =>
          Left(ValidationError("You already joined the table"))
      }
    case JoinLeftCmd.LeftAsPlayer(_) =>
      players.collectFirst{ case p @ HumanPlayer(`user`, _) => p} match {
        case None =>
          Left(ValidationError("You are not player on this table"))
        case Some(player) =>
          val newState = this.copy(players = players - player)
          Right(WsMsgOut.Riichi.PlayerLeftTable(tableId, player), newState)
      }
  }

  def validateCmd(cmd: PlayerCmd[Riichi], sender: Either[ServiceId, UserId]): Either[ValidationError, List[TableEvent[Riichi]]] = cmd match {
    case RiichiWsCmd.StartGame(`tableId`, gameId, config) =>
      if (players.nonEmpty) {
        Right(List(RiichiEvent.GameStarted(tableId, gameId, config)))
      } else {
        Left(ValidationError("No Human players"))
      }
    case _                                                =>
      Left(ValidationError(s"Game is not started, ${cmd.getClass.getSimpleName} is not supported."))
  }

  def applyEvent(e: TableEvent[Riichi]): (List[ScheduledCommand[Riichi]], RiichiTableState) = e match {
    case RiichiEvent.GameStarted(_, gameId, config) =>
      val game: GameStarted = RiichiHelper.initializeHands(this, config, gameId)
      val nextCmd = RiichiWsCmd.GetTileFromWall(tableId, gameId, 1)
      (List(ScheduledCommand(config.nextTileDelay, nextCmd)), game)
    case _                                          => (Nil, this)
  }

  def projection(position: Option[Either[ServiceId, UserId]]): WsMsgOut.Riichi.RiichiState =
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

  def tableCmd(cmd: JoinLeftCmd, user: User): Either[ValidationError, (WsMsgOut, NoGameOnTable)] = cmd match {
    case JoinLeftCmd.JoinAsPlayer(_) =>
      ???
    case JoinLeftCmd.LeftAsPlayer(_) =>
      ???
  }

  def validateCmd(cmd: PlayerCmd[Riichi], position:): Either[ValidationError, List[TableEvent[Riichi]]] = cmd match {
    case _: RiichiWsCmd.StartGame => Left(ValidationError("Game already started"))

    case RiichiWsCmd.DiscardTile(_, _, tile, commandTurn) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
        state <- getPlayerState(sender).asEither(s"No player at $sender")
        _ <- state.currentTile.nonEmpty.asEither("You can't discard tile")
        tileInHand <- (state.closedHand ::: state.currentTile.toList).find(t => t.repr == tile)
          .asEither(s"Tile $tile is not in hand")
        cmds = CommandPredictor.predictsCommandsOnDiscard(this, tileInHand, state.player.position)
      } yield List(RiichiEvent.TileDiscared(tableId, gameId, tileInHand, turn, state.player.position, cmds))

    case RiichiWsCmd.GetTileFromWall(_, _, commandTurn, position) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
        playerState <- getPlayerState(sender).asEither("Player with this position is not found")
        tile <- this.deck.headOption.asEither("Tiles are finished")
        cmds = CommandPredictor.predictCommandsOnTileFromTheWall(this, tile, playerState)
      } yield List(RiichiEvent.TileFromTheWallTaken(tableId, gameId, tile, commandTurn, playerState.player.position, cmds))

    case RiichiWsCmd.DeclareTsumo(_, _, _) =>
      for {
        state <- getPlayerState(sender).asEither(s"No player at $sender")
        newTile <- state.currentTile.asEither("You can declare tsumo only on your tile from the wall")
        handValue <- HandValue.computeTsumoOnTile(newTile, state).asEither("Your hand is not winning.")
      } yield List(RiichiEvent.TsumoDeclared(tableId, gameId, turn, state.player.position))

    case RiichiWsCmd.ScoreGame(_, _)                           =>
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
    case RiichiWsCmd.ClaimPung(_, _, from, commandTurn, tiles) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
        playerState <- getPlayerState(sender).asEither(s"No player at $sender")
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
      val nextCmd = RiichiWsCmd.GetTileFromWall(tableId, gameId, nextTurn, position.nextPosition)
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
      val scoreGame = RiichiWsCmd.ScoreGame(tableId, gameId)
      (List(ScheduledCommand(0 seconds, scoreGame)), this)
    case RiichiEvent.GameScored(_, _, _, score)                       =>
      (Nil, NoGameOnTable(
        this.admin,
        this.tableId,
        this.players,
        this.points.addGameScore(score))
      )
    case RiichiEvent.TileClaimed(_, _, set, position) =>
      (Nil, this)
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
      turn = turn,
      points = this.points
    )

  def players: Set[Player[Riichi]] = playerStates.map(_.player).toSet

  def getPlayer(position: PlayerPosition[Riichi]): Option[Player[Riichi]] = playerStates
    .map(_.player)
    .find(p => p.position == position)

  def getPlayerState(sender: Either[ServiceId, UserId]): Option[PlayerState] = playerStates
    .find(st =>st.player.senderId == sender)

  def getPlayerState(position: PlayerPosition[Riichi]): Option[PlayerState] = playerStates
    .find(st => st.player.position == position)
}