package com.ll.tests.riichitests.fullgame

import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.riichi.{RiichiConfig, TestingState}
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.{WsMsgIn, WsMsgOut}
import com.ll.utils.{CommonData, Test}

import scala.concurrent.duration._

class FullGamePlayTillLastTile extends Test {
  "Start game with 4 players" in new CommonData {
    val st = TestingState(
      eastHand = List(
        "5_wan", "3_pin", "3_pin", "4_pin", "4_pin", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou"
      ),
      southHand = List(
        "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
      ),
      westHand = List(
        "red", "red", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
      ),
      northHand = List(
        "green", "green", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
      ),
      uraDoras = List("north"),
      wall = List("red", "3_pin")
    )

    val player1 = createNewPlayer()
    val player2 = createNewPlayer()
    val player3 = createNewPlayer()
    val player4 = createNewPlayer()
    val player5 = createNewPlayer()

    player1.ws ! WsMsgIn.Ping(23)
    player1.ws.expectWsMsgT[WsMsgOut.Pong]()

    player1.createTable(tableId)
    player1.joinTable(tableId)
    player2.joinTable(tableId)
    player3.joinTable(tableId)
    player4.joinTable(tableId)

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(
      nextTileDelay = 0.millis,
      testingTiles = Some(st)
    )))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    var tileToDiscard = st.eastHand.head
    0 until 17 foreach {round =>
      println(s"Round $round")
      player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, tileToDiscard, 8 * round + 1)
      player2.ws.expectWsMsg {
        case discarded: WsMsgOut.Riichi.TileDiscarded if discarded.position == RiichiPosition.EastPosition =>
          discarded.tile should be(tileToDiscard)
          discarded
      }

      val southTaken =  player2.ws.expectWsMsg {
        case taken: WsMsgOut.Riichi.TileFromWallTaken if taken.position == RiichiPosition.SouthPosition  =>
          taken
      }
      player2.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, southTaken.tile, 8 * round + 3)
      player3.ws.expectWsMsg {
        case discarded: WsMsgOut.Riichi.TileDiscarded if discarded.position == RiichiPosition.SouthPosition =>
          discarded.tile should be(southTaken.tile)
          discarded
      }

      val westTaken =  player3.ws.expectWsMsg {
        case taken: WsMsgOut.Riichi.TileFromWallTaken if taken.position == RiichiPosition.WestPosition =>
          taken
      }
      player3.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, westTaken.tile, 8 * round + 5)
      player4.ws.expectWsMsg {
        case discarded: WsMsgOut.Riichi.TileDiscarded if discarded.position == RiichiPosition.WestPosition =>
          discarded.tile should be(westTaken.tile)
          discarded
      }

      val northTaken =  player4.ws.expectWsMsg {
        case taken: WsMsgOut.Riichi.TileFromWallTaken  if taken.position == RiichiPosition.NorthPosition =>
          taken
      }
      player4.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, northTaken.tile, 8 * round + 7)
      player1.ws.expectWsMsg {
        case discarded: WsMsgOut.Riichi.TileDiscarded if discarded.position == RiichiPosition.NorthPosition =>
          discarded.tile should be(northTaken.tile)
          discarded
      }

      val eastTaken =  player1.ws.expectWsMsg {
        case taken: WsMsgOut.Riichi.TileFromWallTaken  =>
          taken.position should be (RiichiPosition.EastPosition)
          taken
      }
      tileToDiscard = eastTaken.tile
    }
    player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, tileToDiscard, 8 * 17 + 1)
    player2.ws.expectWsMsg {
      case discarded: WsMsgOut.Riichi.TileDiscarded if discarded.position == RiichiPosition.EastPosition =>
        discarded.tile should be(tileToDiscard)
        discarded
    }

    val lastSouthTile =  player2.ws.expectWsMsg {
      case taken: WsMsgOut.Riichi.TileFromWallTaken  =>
        taken.position should be (RiichiPosition.SouthPosition)
        taken
    }
    player2.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, lastSouthTile.tile, 8 * 17 + 3)
    player1.ws.expectWsMsg {
      case discarded: WsMsgOut.Riichi.TileDiscarded if discarded.position == RiichiPosition.SouthPosition =>
        discarded.tile should be(lastSouthTile.tile)
        discarded
    }

    player1.ws.expectWsMsg {
      case drawDeclared: WsMsgOut.Riichi.DrawDeclared =>
        drawDeclared
    }
    player1.ws.expectWsMsg {
      case drawDeclared: WsMsgOut.Riichi.GameScored =>
        drawDeclared
    }


    player1.ws ! WsRiichiCmd.GetState(tableId)
    val finalState = player1.ws.expectWsMsg {
      case state: WsMsgOut.Riichi.RiichiState =>
        state
    }

    finalState.states.foreach{
      case pst if pst.player.position == RiichiPosition.EastPosition =>
        pst.closedHand should contain theSameElementsAs st.eastHand.tail
      case pst if pst.player.position == RiichiPosition.SouthPosition =>
        pst.closedHand should contain theSameElementsAs st.southHand
      case pst if pst.player.position == RiichiPosition.WestPosition =>
        pst.closedHand should contain theSameElementsAs st.westHand
      case pst if pst.player.position == RiichiPosition.NorthPosition =>
        pst.closedHand should contain theSameElementsAs st.northHand
    }
  }
}
