package com.ll.domain.games.riichi

import com.ll.domain.games.GameId
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.AIPlayer
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.position.PlayerPosition.RiichiPosition

object RiichiHelper {
  def initializeHands(table: NoGameOnTable, config: RiichiConfig, gameId: GameId) = {
    def generatePlayer(position: PlayerPosition[Riichi]) = table.players.find(_.position == position)
      .getOrElse(AIPlayer(config.defaultEastAi, position))

    val allTiles = TestHelper.prepareTiles(config.testingTiles)
    val (eastHand, rem1) = allTiles.splitAt(13)
    val (southHand, rem2) = rem1.splitAt(13)
    val (westHand, rem3) = rem2.splitAt(13)
    val (northHand, rem4) = rem3.splitAt(13)
    val (uraDoras, remaining) = rem4.splitAt(1)

    val east = generatePlayer(RiichiPosition.EastPosition)
    val south = generatePlayer(RiichiPosition.SouthPosition)
    val west = generatePlayer(RiichiPosition.WestPosition)
    val north = generatePlayer(RiichiPosition.NorthPosition)

    GameStarted(
      admin = table.admin,
      tableId = table.tableId,
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
      config = config,
      table.points
    )
  }
}
