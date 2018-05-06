package com.ll.tests.riichitests

import com.ll.domain.auth.UserId
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.{WsMsgIn, WsMsgOut}
import com.ll.utils.{CommonData, Test}

class PlayersJoinLeftTableTest extends Test {

  "Join game" in new CommonData {
    val player1 = createNewPlayer()
    val player2 = createNewPlayer()
    val player3 = createNewPlayer()
    val player4 = createNewPlayer()
    val player5 = createNewPlayer()

    player1.ws ! WsMsgIn.Ping(23)
    player1.ws.expectWsMsgT[WsMsgOut.Pong]()

    player1.createTable(tableId)

    player1.ws ! WsRiichiCmd.JoinAsPlayer(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.PlayerJoinedTable]()

    player2.ws ! WsRiichiCmd.JoinAsPlayer(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.PlayerJoinedTable]()

    player3.ws ! WsRiichiCmd.JoinAsPlayer(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.PlayerJoinedTable]()

    player4.ws ! WsRiichiCmd.JoinAsPlayer(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.PlayerJoinedTable]()

    player5.ws ! WsRiichiCmd.JoinAsPlayer(tableId)
    player5.ws.expectWsMsgT[WsMsgOut.ValidationError]()
  }

  "Join & left game" in new CommonData {
    val player1 = createNewPlayer()
    val player2 = createNewPlayer()

    player1.ws ! WsMsgIn.Ping(23)
    player1.ws.expectWsMsgT[WsMsgOut.Pong]()

    player1.createTable(tableId)

    player1.ws ! WsRiichiCmd.GetState(tableId)
    player1.ws.expectWsMsg{
      case state: WsMsgOut.Riichi.RiichiState =>
        state.states shouldBe empty
        state
    }

    player1.ws ! WsRiichiCmd.JoinAsPlayer(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.PlayerJoinedTable]()

    player1.ws ! WsRiichiCmd.GetState(tableId)
    player1.ws.expectWsMsg{
      case state: WsMsgOut.Riichi.RiichiState if state.states.size == 1 => state
    }


    player2.ws ! WsRiichiCmd.JoinAsPlayer(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.PlayerJoinedTable]()

    player1.ws ! WsRiichiCmd.GetState(tableId)
    player1.ws.expectWsMsg{
      case state: WsMsgOut.Riichi.RiichiState  =>
        state.states.size should be (2)
        state
    }

    player2.ws ! WsRiichiCmd.LeftAsPlayer(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.PlayerLeftTable]()

    player1.ws ! WsRiichiCmd.GetState(tableId)
    player1.ws.expectWsMsg{
      case state: WsMsgOut.Riichi.RiichiState  =>
        state.states.size should be (1)
        state
    }

  }
}
