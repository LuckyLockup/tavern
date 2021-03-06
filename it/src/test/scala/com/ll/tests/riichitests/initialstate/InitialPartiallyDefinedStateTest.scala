package com.ll.tests.riichitests.initialstate

import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.riichi.{RiichiConfig, TestingState}
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.WsMsgOut
import com.ll.utils.{CommonData, Test}

class InitialPartiallyDefinedStateTest extends Test{
  "Test initial state with predefined tiles" in new CommonData {
    val player1 = createNewPlayer()
    val player2 = createNewPlayer()
    val player3 = createNewPlayer()
    val player4 = createNewPlayer()

    player1.createTable(tableId)
    player1.createTable(tableId)
    player1.joinTable(tableId)
    player2.joinTable(tableId)
    player3.joinTable(tableId)
    player4.joinTable(tableId)

    val st = TestingState(
      eastHand = List(
        "5_wan", "3_pin", "3_pin"
      ),
      southHand = List(
        "1_wan", "2_wan"
      ),
      westHand = List(
        "red", "red", "3_wan", "4_wan"
      ),
      northHand = List(
        "green", "green", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
      ),
      uraDoras = List(),
      wall = List("red", "3_pin", "5_pin")
    )

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(testingTiles = Some(st))))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.GetState(tableId)
    player1.ws.expectWsMsg {
      case state: WsMsgOut.Riichi.RiichiState =>
        state.turn should be (1)
        state.uraDoras.size should be (1)
        state.uraDoras.head should not contain "x"
        state.states.foreach{
          case pst if pst.player.position == PlayerPosition.RiichiPosition.EastPosition =>
            pst.currentTile should contain (st.eastHand.head)
            pst.closedHand.size should be (13)
            pst.closedHand should contain allElementsOf (st.eastHand.tail)
            pst.discard shouldBe empty
          case pst =>
            pst.closedHand should contain theSameElementsAs Array.fill(13)("x")
            pst.currentTile shouldBe empty
            pst.discard shouldBe empty
            pst.currentTile shouldBe empty
        }
        state
    }

    player2.ws ! WsRiichiCmd.GetState(tableId)
    player2.ws.expectWsMsg {
      case state: WsMsgOut.Riichi.RiichiState =>
        state.turn should be (1)
        state.uraDoras.size should be (1)
        state.uraDoras.head should not contain "x"
        state.states.foreach{
          case pst if pst.player.position == PlayerPosition.RiichiPosition.SouthPosition =>
            pst.currentTile shouldBe empty
            pst.closedHand.size should be (13)
            pst.closedHand should contain allElementsOf (st.southHand)
            pst.discard shouldBe empty
          case pst =>
            pst.closedHand should contain theSameElementsAs Array.fill(13)("x")
            if (pst.player.position == PlayerPosition.RiichiPosition.EastPosition) {
              pst.currentTile should contain ("x")
            } else {
              pst.currentTile shouldBe empty
            }
            pst.discard shouldBe empty
        }
        state
    }

    player3.ws ! WsRiichiCmd.GetState(tableId)
    player3.ws.expectWsMsg {
      case state: WsMsgOut.Riichi.RiichiState =>
        state.turn should be (1)
        state.uraDoras.size should be (1)
        state.uraDoras.head should not contain "x"
        state.states.foreach{
          case pst if pst.player.position == PlayerPosition.RiichiPosition.WestPosition =>
            pst.currentTile shouldBe empty
            pst.closedHand.size should be (13)
            pst.closedHand should contain allElementsOf (st.westHand)
            pst.discard shouldBe empty
          case pst =>
            pst.closedHand should contain theSameElementsAs Array.fill(13)("x")
            if (pst.player.position == PlayerPosition.RiichiPosition.EastPosition) {
              pst.currentTile should contain ("x")
            } else {
              pst.currentTile shouldBe empty
            }
            pst.discard shouldBe empty
        }
        state
    }

    player4.ws ! WsRiichiCmd.GetState(tableId)
    player4.ws.expectWsMsg {
      case state: WsMsgOut.Riichi.RiichiState =>
        state.turn should be (1)
        state.uraDoras.size should be (1)
        state.uraDoras.head should not contain "x"
        state.states.foreach{
          case pst if pst.player.position == PlayerPosition.RiichiPosition.NorthPosition =>
            pst.currentTile shouldBe empty
            pst.closedHand.size should be (13)
            pst.closedHand should contain allElementsOf (st.northHand)
            pst.discard shouldBe empty
          case pst =>
            pst.closedHand should contain theSameElementsAs Array.fill(13)("x")
            if (pst.player.position == PlayerPosition.RiichiPosition.EastPosition) {
              pst.currentTile should contain ("x")
            } else {
              pst.currentTile shouldBe empty
            }
            pst.discard shouldBe empty
        }
        state
    }
  }
}
