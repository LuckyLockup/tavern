package com.ll.riichitests

import com.ll.domain.auth.UserId
import com.ll.domain.games.riichi.RiichiConfig
import com.ll.domain.ws.WsMsgIn.{RiichiGameCmd, UserCmd}
import com.ll.domain.ws.WsMsgOut
import com.ll.utils.{CommonData, Test}

class RiichiRonTest extends Test {
  "Basic game play" in new CommonData {
    val player1 = createNewPlayer(UserId(101))

    player1.createTable(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.RiichiState]()

    player1.ws ! UserCmd.JoinAsPlayer(tableId, player1.user)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.PlayerJoinedTable]()

    val eastHand = List(
      "2_pin", "3_pin", "red", "4_pin", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou"
    )
    player1.ws ! RiichiGameCmd.StartGame(tableId, gameId, RiichiConfig().copy(testingTiles =
      eastHand
    ))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! UserCmd.GetState(tableId, player1.userId)
    val state1: WsMsgOut.Riichi.RiichiState = player1.ws.expectWsMsg {
      case state: WsMsgOut.Riichi.RiichiState =>
        state.states.size should be(4)
        state.states.head.closedHand should contain theSameElementsAs eastHand
        state
    }
  }
}
