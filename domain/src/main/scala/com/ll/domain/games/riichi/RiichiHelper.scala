package com.ll.domain.games.riichi

import com.ll.domain.auth.User
import com.ll.domain.games.{GameId, TableId}
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.{AIPlayer, HumanPlayer}
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.position.PlayerPosition.RiichiPosition


object RiichiHelper {
  def initializeHands(
    tableId: TableId,
    gameId: GameId,
    admin: User,
    config: RiichiConfig,
    humanPlayers: Set[HumanPlayer[Riichi]]) = {
    def generatePlayer(position: PlayerPosition[Riichi]) = humanPlayers.find(_.position == position)
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
    game
  }
}