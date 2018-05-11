package com.ll.tests.riichitests

import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.riichi.{RiichiConfig, TestingState}
import com.ll.domain.games.riichi.result.{HandValue, Points}
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.WsMsgOut
import com.ll.utils.{CommonData, Test}

class TsumoTest extends Test {
  "Declaring tsumo" in new CommonData {
    val player1 = createNewPlayer()

    player1.createTable(tableId)

    player1.ws ! WsRiichiCmd.JoinAsPlayer(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.PlayerJoinedTable]()
    val st = TestingState(
      eastHand = List(
        "2_pin", "3_pin", "3_pin", "4_pin", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou"
      ),
      southHand = List(
        "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
      ),
      westHand = List(
        "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
      ),
      uraDoras = List("2_sou"),
      wall = List("3_pin")
    )

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(testingTiles =
      Some(st)
    )))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.GetState(tableId)
    val state1: WsMsgOut.Riichi.RiichiState = player1.ws.expectWsMsg {
      case state: WsMsgOut.Riichi.RiichiState =>
        state.states.size should be(4)
        state.uraDoras should contain theSameElementsAs st.uraDoras
        state.states.head.closedHand should contain theSameElementsAs st.eastHand
        state
    }
    player1.ws ! tileFromTheWall.commands.head.asInstanceOf[WsRiichiCmd.DeclareTsumo].copy(approxHandValue = None)

    player1.ws.expectWsMsg {
      case tsumo: WsMsgOut.Riichi.TsumoDeclared =>
        tsumo.position should be(RiichiPosition.EastPosition)
        tsumo.turn should be(2)
        tsumo
    }
    val score = player1.ws.expectWsMsg {
      case score: WsMsgOut.Riichi.GameScored =>
        score
    }

    player1.ws ! WsRiichiCmd.GetState(tableId)
    player1.ws.expectWsMsg {
      case state: WsMsgOut.Riichi.RiichiState =>
        state.turn should be(0)
        state.points.points(PlayerPosition.RiichiPosition.EastPosition) should be (Points(26000))
        state
    }
  }
}
