package com.ll.domain.games.riichi

import com.ll.domain.Const
import com.ll.domain.auth.User
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.deck.{DeclaredSet, DiscardedTile, Tile}
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.position.{PlayerPosition, PositionUtility}
import com.ll.domain.games.riichi.initialization.RiichiHelper
import com.ll.domain.games.riichi.result.{GameScore, HandValue, TablePoints}
import com.ll.domain.games.{GameId, Player, ScheduledCommand, TableId}
import com.ll.domain.persistence._
import com.ll.domain.ops.EitherOps._
import com.ll.domain.persistence.TableCmd.RiichiCmd
import com.ll.domain.ws.WsMsgOut
import com.ll.domain.ws.WsMsgOut.ValidationError
import com.ll.domain.ws.WsRiichi.{RiichiPlayerState, WsDeclaredSet}

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
        case None if user.isRight =>
          //TODO refactor for AI
          PositionUtility
            .addUser(players, user.right.get)
            .map(player => List(RiichiEvent.PlayerJoined(tableId, player)))
        case Some(_)              =>
          Left(ValidationError("You already joined the table"))
      }

    case TableCmd.LeftAsPlayer(_, user)                 =>
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
      val nextAutoCmd = RiichiCmd.GetTileFromTheWall(tableId, gameId, 1, RiichiPosition.EastPosition)
      (List(ScheduledCommand(config.turnDuration, nextAutoCmd)), game)

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
  deadWall: List[Tile],
  deck: List[Tile],
  turn: Int = 1,
  config: RiichiConfig,
  points: TablePoints,
  possibleCmds: List[RiichiCmd] = Nil,
  pendingEvents: Option[PendingEvents] = None,
  windOfRound: PlayerPosition[Riichi] = RiichiPosition.EastPosition,
  gameScore: Option[GameScore] = None
) extends RiichiTableState {

  def validateCmd(cmd: TableCmd[Riichi]): Either[WsMsgOut, List[TableEvent[Riichi]]] = cmd match {
    case _: RiichiCmd.StartGame =>
      for {
        _ <- gameScore.nonEmpty.asEither("Game is not scored.")
      } yield List(RiichiEvent.GameStarted(tableId, gameId, config))

    case RiichiCmd.GetTileFromTheWall(_, _, commandTurn, position) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
      } yield {
        pendingEvents match {
          case None          => List(this.nextTileEvent(turn, position))
          case Some(pending) => ClaimConflictHelper.resolveEvents(pending, this.config)
        }
      }

    case RiichiCmd.DeclareTsumo(_, _, commandTurn, position, _) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
        state = getPlayerState(position)
        newTile <- state.currentTile.asEither("You can declare tsumo only on your tile from the wall")
        handValue <- state.tsumoOn(newTile.repr).asEither("Your hand is not winning.")
      } yield List(RiichiEvent.TsumoDeclared(tableId, gameId, turn, state.player.position))

    case RiichiCmd.DiscardTile(_, _, commandTurn, position, tile) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
        state = getPlayerState(position)
        _ <- state.shouldDiscardTile(turn).asEither("You can't discard tile")
        tileInHand <- (state.closedHand ::: state.currentTile.toList).find(t => t.repr == tile)
          .asEither(s"Tile $tile is not in hand")
        cmds = predictsCommandsOnDiscard(DiscardedTile(tileInHand, turn, position))
      } yield List(RiichiEvent.TileDiscared(tableId, gameId, turn, position, tileInHand, cmds))

    case RiichiCmd.SkipAction(_, _, commandTurn, position) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
        _ <- this.possibleCmds.exists(cmd => cmd.position == position).asEither(s"No actions to skip at $position")
        pending <- this.pendingEvents.asEither("No pending events")
      } yield {
        val actionSkipped = RiichiEvent.ActionSkipped(tableId, gameId, turn, position)
        this.possibleCmds.filter(cmd => cmd.position != position) match {
          case Nil => actionSkipped :: ClaimConflictHelper.resolveEvents(pending, this.config)
          case _   => List(actionSkipped)
        }
      }

    case RiichiCmd.ClaimPung(_, _, commandTurn, position) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
        playerState = getPlayerState(position)
        discardedTile <- discardToClaim().find(d => d.position != position).asEither("Discarded tile is not found")
        pung <- playerState.pungOn(discardedTile.tile).asEither(s"Can't claim pung on ${discardedTile.tile.repr}")
      } yield {
        val pungTiles = List(pung.x, pung.y, pung.z).filter(t => t != discardedTile.tile)
        val pungClaimed = RiichiEvent.PungClaimed(tableId, gameId, turn, position, discardedTile.position, discardedTile.tile, pungTiles)
        this.pendingEvents match {
          case None           => List(pungClaimed)
          case Some(pendging) => ClaimConflictHelper.resolvePung(this.possibleCmds, pendging, this.config, pungClaimed)
        }
      }

    case RiichiCmd.ClaimChow(_, _, commandTurn, position, onTile, tiles) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
        playerState = getPlayerState(position)
        discardedTile <- discardToClaim().find(d => d.tile.repr == onTile).asEither("Discarded tile is not found")
        _ <- (position == discardedTile.position.nextPosition).asEither("You can only take Chow from the previous player")
        chowOpt = playerState.chowsOn(discardedTile.tile).find(chow => chow.tiles.toSet == (onTile :: tiles).toSet)
        chow <- chowOpt.asEither(s"Can't claim chow with $tiles")
      } yield {
        val otherTiles = List(chow.x, chow.y, chow.z).filter(t => t != discardedTile.tile)
        val chowClaimed = RiichiEvent.ChowClaimed(tableId, gameId, turn, position, discardedTile.position, discardedTile.tile, otherTiles)
        this.pendingEvents match {
          case None           => List(chowClaimed)
          case Some(pendging) => ClaimConflictHelper.resolveChow(this.possibleCmds, pendging, this.config, chowClaimed)
        }
      }

    case RiichiCmd.DeclareRon(_, _, commandTurn, position, _) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
        playerState = getPlayerState(position)
        discardedTile <- discardToClaim().find(d => d.position != position).asEither("Discarded tile is not found")
        ronValue <- playerState.ronOn(discardedTile.tile).asEither(s"Can't ron on ${discardedTile.tile.repr}")
      } yield {
        val ronDeclared = RiichiEvent.RonDeclared(tableId, gameId, turn, position, discardedTile.position)
        this.pendingEvents match {
          case None           => List(ronDeclared)
          case Some(pendging) => ClaimConflictHelper.resolveRon(this.possibleCmds, pendging, this.config, ronDeclared)
        }
      }

    case RiichiCmd.ScoreGame(_, _, commandTurn) =>
      for {
        _ <- (commandTurn == turn).asEither(s"Actual turn $turn, but command turn $commandTurn")
      } yield  {
        val gameScore = GameScore(Right(RiichiPosition.EastPosition), Map())
        List(RiichiEvent.GameScored(tableId, gameId, turn, gameScore))
      }

  }

  def applyEvent(e: TableEvent[Riichi]): (List[ScheduledCommand[Riichi]], RiichiTableState) = e match {
    case RiichiEvent.ActionSkipped(_, _, _, position) =>
      (Nil, this.copy(possibleCmds = possibleCmds.filter(c => c.position != position)))

    case RiichiEvent.PendingEvent(ev) =>
      val newPending = pendingEvents.map(pending =>
        ev match {
          case ron: RiichiEvent.RonDeclared  => pending.copy(rons = ron :: pending.rons)
          case pung: RiichiEvent.PungClaimed => pending.copy(pung = Some(pung))
          case chow: RiichiEvent.ChowClaimed => pending.copy(chow = Some(chow))
          case _                             => pending
        }
      )
      (Nil, this.copy(
        pendingEvents = newPending,
        possibleCmds = possibleCmds.filter(c => c.position != ev.position
        )))

    case RiichiEvent.TileDiscared(_, _, _, position, tile, actions) =>
      val updatedStates = this.playerStates.map {
        case st if st.player.position != position => st
        case st if st.player.position == position =>
          val newClosedHand = ((st.currentTile.toList ::: st.closedHand).toSet - tile).toList
          val newDiscard = DiscardedTile(tile, turn, position) :: st.discard
          st.copy(
            closedHand = newClosedHand.sortBy(_.order),
            discard = newDiscard,
            currentTile = None
          )
      }
      val nextTurn = this.turn + 1
      val nextAutoCmd = RiichiCmd.GetTileFromTheWall(tableId, gameId, nextTurn, position.nextPosition)
      val nextEvent = this.nextTileEvent(nextTurn, position.nextPosition)
      if (actions.isEmpty) {
        val updatedState = this.copy(
          playerStates = updatedStates,
          turn = nextTurn,
          possibleCmds = Nil,
          pendingEvents = None
        )
        (List(ScheduledCommand(0.seconds, nextAutoCmd)), updatedState)
      } else {
        val updatedState = this.copy(
          playerStates = updatedStates,
          turn = nextTurn,
          possibleCmds = actions,
          pendingEvents = Some(PendingEvents(nextEvent))
        )
        (List(ScheduledCommand(config.nextTileDelay, nextAutoCmd)), updatedState)
      }

    case RiichiEvent.TileFromTheWallTaken(_, _, _, position, tile, _) =>
      val updatedStates = this.playerStates.map {
        case st if st.player.position != position => st
        case st if st.player.position == position => st.copy(currentTile = Some(tile))
      }
      val updatedState = this.copy(
        pendingEvents = None,
        possibleCmds = Nil,
        playerStates = updatedStates,
        turn = this.turn + 1,
        deck = deck.drop(1)
      )
      val autoDiscard = RiichiCmd.DiscardTile(tableId, gameId, turn + 1, position, tile.repr)
      (List(ScheduledCommand(config.turnDuration, autoDiscard)), updatedState)

    case RiichiEvent.GameScored(_, _, _, score) =>
      (Nil, this.copy(gameScore = Some(score)))

    case RiichiEvent.PungClaimed(_, _, _, position, from, claimedTile, tiles) =>
      val updatedStates = this.playerStates.map {
        case st if st.player.position == from     => st.copy(discard = st.discard.tail)
        case st if st.player.position == position =>
          val openedSet = DeclaredSet(claimedTile, tiles, from, turn)
          st.copy(
            closedHand = st.closedHand.filterNot(t => tiles.contains(t)),
            openHand = openedSet :: st.openHand
          )
        case st => st
      }
      val updatedState = this.copy(
        pendingEvents = None,
        possibleCmds = Nil,
        playerStates = updatedStates,
        turn = this.turn + 1,
        deck = deck
      )
      val autoDiscard = RiichiCmd.DiscardTile(
        tableId,
        gameId,
        turn + 1,
        position,
        getPlayerState(position).closedHand.head.repr)
      (List(ScheduledCommand(config.turnDuration, autoDiscard)), updatedState)

    case ev: RiichiEvent.SetClaimed =>
      val updatedStates = this.playerStates.map {
        case st if st.player.position == ev.from     => st.copy(discard = st.discard.tail)
        case st if st.player.position == ev.position =>
          val openedSet = DeclaredSet(ev.claimedTile, ev.tiles, ev.from, turn)
          st.copy(
            closedHand = st.closedHand.filterNot(t => ev.tiles.contains(t)),
            openHand = openedSet :: st.openHand
          )
        case st => st
      }
      val updatedState = this.copy(
        pendingEvents = None,
        possibleCmds = Nil,
        playerStates = updatedStates,
        turn = this.turn + 1,
        deck = deck
      )
      val autoDiscard = RiichiCmd.DiscardTile(
        tableId,
        gameId,
        turn + 1,
        ev.position,
        getPlayerState(ev.position).closedHand.head.repr)
      (List(ScheduledCommand(config.turnDuration, autoDiscard)), updatedState)

    case RiichiEvent.DrawDeclared(_, _, _) =>
      val scoreGame = RiichiCmd.ScoreGame(tableId, gameId, turn + 1)
      (List(ScheduledCommand(0.seconds, scoreGame)), this.copy(turn = turn + 1))

    case RiichiEvent.TsumoDeclared(_, _, _, position) =>
      //TODO open doras
      val scoreGame = RiichiCmd.ScoreGame(tableId, gameId, turn + 1)
      (List(ScheduledCommand(0.seconds, scoreGame)), this.copy(turn = turn + 1))

    case RiichiEvent.RonDeclared(_, _, _, position, from) =>
      //TODO open doras
      val scoreGame = RiichiCmd.ScoreGame(tableId, gameId, turn + 1)
      (List(ScheduledCommand(0.seconds, scoreGame)), this.copy(turn = turn + 1))
  }

  def projection(position: Option[PlayerPosition[Riichi]]): WsMsgOut.Riichi.RiichiState =
    WsMsgOut.Riichi.RiichiState(
      tableId = tableId,
      admin = admin,
      states = playerStates.map {
        case state =>
          val hideTiles = (tile: Tile) => if (position.contains(state.player.position) || gameScore.nonEmpty) tile.repr else Const.ClosedTile
          RiichiPlayerState(
            player = state.player,
            closedHand = state.closedHand.sortBy(_.order).map(hideTiles),
            openHand = state.openHand.map(set => WsDeclaredSet(set)),
            currentTile = state.currentTile.map(hideTiles),
            discard = state.discard.map(_.tile.repr),
            online = state.online
          )
      },
      uraDoras = uraDoras.map(_.repr),
      deck = deck.size,
      turn = turn,
      points = this.points
    )

  def players: Set[Player[Riichi]] = playerStates.map(_.player).toSet

  def getPlayerState(position: PlayerPosition[Riichi]): PlayerState = playerStates
    .find(st => st.player.position == position).get

  def discardToClaim(): Option[DiscardedTile[Riichi]] = {
    val hasToDiscardFirst = playerStates
      .exists(st => st.shouldDiscardTile(turn))
    if (hasToDiscardFirst) {
      None
    } else {
      playerStates
        .flatMap(_.discard.headOption)
        .sortBy(_.turn)
        .lastOption
    }
  }

  def nextTileEvent(forTurn: Int, forPosition: PlayerPosition[Riichi]): TableEvent[Riichi] = deck match {
    case Nil          => RiichiEvent.DrawDeclared(tableId, gameId, turn)
    case tile :: tail =>
      RiichiEvent.TileFromTheWallTaken(
        tableId,
        gameId,
        forTurn,
        forPosition,
        tile,
        predictCommandsOnTileFromTheWall(tile.repr, forPosition)
      )
  }

  def predictCommandsOnTileFromTheWall(tile: String, position: PlayerPosition[Riichi]): List[RiichiCmd] = {
    val playerState = getPlayerState(position)
    val tsumo = playerState.tsumoOn(tile)
      .toList
      .map(v => RiichiCmd.DeclareTsumo(tableId, gameId, this.turn + 1, playerState.player.position, Some(v)))
    //TODO open kong
    //TODO declare riichi
    tsumo
  }

  def updateTurn(ev: TableEvent[Riichi]): Int = ev match {
    case _: RiichiEvent.ChowClaimed | _: RiichiEvent.PungClaimed |
         _: RiichiEvent.RonDeclared | _: RiichiEvent.TsumoDeclared |
         _: RiichiEvent.TileFromTheWallTaken | _: RiichiEvent.TileDiscared => turn + 1
    case _                                                                 => turn
  }

  def predictsCommandsOnDiscard(discardedTile: DiscardedTile[Riichi]): List[RiichiCmd] = {
    val nextTurn = discardedTile.turn + 1
    this.playerStates
      //commands are predicted on player discard. The player who discarded can't take tile.
      .filter(st => st.player.position != discardedTile.position)
      .flatMap { st =>
        val declarePungs: Option[RiichiCmd.ClaimPung] = st.pungOn(discardedTile.tile)
          .map(pung => RiichiCmd.ClaimPung(
            tableId,
            gameId,
            nextTurn,
            st.player.position)
          )

        val declareChow = if (discardedTile.position.nextPosition == st.player.position) {
          st.chowsOn(discardedTile.tile).map(chow => RiichiCmd.ClaimChow(
            tableId,
            gameId,
            nextTurn,
            st.player.position,
            discardedTile.tile.repr,
            chow.tiles.filter(t => t != discardedTile.tile.repr))
          )
        } else {
          Nil
        }

        val declareRon = st.ronOn(discardedTile.tile).map(handValue =>
          RiichiCmd.DeclareRon(
            tableId,
            gameId,
            nextTurn,
            st.player.position,
            Some(handValue)
          )
        )
        declarePungs.toList ::: declareChow ::: declareRon.toList
      }
  }
}