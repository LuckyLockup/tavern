package com.ll.tests.riichitests.fullgame

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.riichi.{RiichiConfig, TestingState}
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.{WsMsgIn, WsMsgOut}
import com.ll.utils.{CommonData, PlayerProbe, Test}

import scala.concurrent.duration._

class SinglePlayerFullGameTest extends Test {
  "Play full game with one player" in new CommonData {
    val st = TestingState(
      eastHand = List(
        "5_wan", "3_pin", "3_pin", "4_pin", "4_pin", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou"
      ),
      uraDoras = List("north"),
      wall = List("red", "3_pin", "4_pin", "3_sou")
    )

    val player1 = createNewPlayer()

    def expectDiscardedTileAndSkipActions(position: PlayerPosition[Riichi]): Unit = {
      player1.ws.expectWsMsg {
        case discarded: WsMsgOut.Riichi.TileDiscarded if discarded.position == position =>
          if (discarded.commands.nonEmpty) {
            player1.ws ! WsRiichiCmd.SkipAction(tableId, gameId, discarded.turn + 1)
            player1.ws.expectWsMsgT[WsMsgOut.Riichi.ActionSkipped]()
          }
          discarded
      }
    }


    player1.ws ! WsMsgIn.Ping(23)
    player1.ws.expectWsMsgT[WsMsgOut.Pong]()

    player1.createTable(tableId)
    player1.joinTable(tableId)

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(
      nextTileDelay = 30.seconds,
      testingTiles = Some(st)
    )))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    var tileToDiscard = st.eastHand.head
    0 until 17 foreach {round =>
      println(s"Round $round")
      player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, tileToDiscard, 8 * round + 1)
      expectDiscardedTileAndSkipActions(RiichiPosition.EastPosition)
      expectDiscardedTileAndSkipActions(RiichiPosition.SouthPosition)
      expectDiscardedTileAndSkipActions(RiichiPosition.WestPosition)
      expectDiscardedTileAndSkipActions(RiichiPosition.NorthPosition)


      val eastTaken =  player1.ws.expectWsMsg {
        case taken: WsMsgOut.Riichi.TileFromWallTaken  =>
          taken.position should be (RiichiPosition.EastPosition)
          taken
      }
      tileToDiscard = eastTaken.tile
    }
    println("Skipping...")
    player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, tileToDiscard, 8 * 17 + 1)
    expectDiscardedTileAndSkipActions(RiichiPosition.EastPosition)
    expectDiscardedTileAndSkipActions(RiichiPosition.SouthPosition)

    player1.ws.expectWsMsg {
      case drawDeclared: WsMsgOut.Riichi.DrawDeclared =>
        drawDeclared
    }
    player1.ws.expectWsMsg {
      case drawDeclared: WsMsgOut.Riichi.GameScored =>
        drawDeclared
    }
  }
}
