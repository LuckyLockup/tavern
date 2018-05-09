package com.ll.domain.games.riichi.initialization

import com.ll.domain.games.GameId
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.AIPlayer
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.riichi._

object RiichiHelper {
  def initializeHands(table: NoGameOnTable, config: RiichiConfig, gameId: GameId) = {
    def generatePlayer(position: PlayerPosition[Riichi]) = table.players.find(_.position == position)
      .getOrElse(AIPlayer(config.defaultEastAi, position))

    val initial = InitialState(config.testingTiles.getOrElse(TestingState()))

    val east = generatePlayer(RiichiPosition.EastPosition)
    val south = generatePlayer(RiichiPosition.SouthPosition)
    val west = generatePlayer(RiichiPosition.WestPosition)
    val north = generatePlayer(RiichiPosition.NorthPosition)

    GameStarted(
      admin = table.admin,
      tableId = table.tableId,
      gameId = gameId,
      playerStates = List(
        PlayerState(player = east, closedHand = initial.eastHand.drop(1), currentTile = initial.eastHand.headOption),
        PlayerState(south, initial.southHand),
        PlayerState(west, initial.westHand),
        PlayerState(north, initial.northHand)
      ),
      uraDoras = initial.uraDoras,
      deadWall = initial.deadWall,
      deck = initial.wall,
      turn = 1,
      config = config,
      table.points
    )
  }
}
