package com.ll.tests.riichitests

import com.ll.domain.auth.UserId
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.riichi.{RiichiConfig, TestingState}
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.WsMsgOut
import com.ll.utils.{CommonData, Test}

class RiichiRonTest extends Test {
  "Declaring riichi with ron" in new CommonData {
    val player1 = createNewPlayer()

    player1.createTable(tableId)
    player1.joinTable(tableId)

    val st = TestingState(
      eastHand = List(
        "2_pin", "3_pin", "red", "4_pin", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou"
      ),
      southHand = List(
        "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
      ),
      westHand = List(
        "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
      ),
      northHand = List(
        "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
      ),
      uraDoras = List("2_sou"),
      wall = List("3_pin")
    )

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(testingTiles = Some(st))))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.GetState(tableId)
    val state1: WsMsgOut.Riichi.RiichiState = player1.ws.expectWsMsg {
      case state: WsMsgOut.Riichi.RiichiState =>
        state.states.size should be(4)
        state.uraDoras should contain theSameElementsAs st.uraDoras
        state.states.head.closedHand should contain theSameElementsAs st.eastHand
        state
    }
    val tileFromTheWall =  player1.ws.expectWsMsg {
      case fromTheWall: WsMsgOut.Riichi.TileFromWallTaken =>
        fromTheWall.position should be (RiichiPosition.EastPosition)
        fromTheWall.tile should be (st.wall.head)
        fromTheWall
    }
  }
}
