package com.ll.riichitests

import com.ll.domain.auth.UserId
import com.ll.domain.games.riichi.RiichiConfig
import com.ll.domain.messages.WsMsg
import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.persistence.{RiichiGameCmd, UserCmd}
import com.ll.utils.{CommonData, Test}

class GameFlowTest extends Test{
  "Start game with 4 players" in new CommonData {
    val player1 = createNewPlayer(UserId(101))
    val player2 = createNewPlayer(UserId(102))
    val player3 = createNewPlayer(UserId(103))
    val player4 = createNewPlayer(UserId(104))
    val player5 = createNewPlayer(UserId(105))

    player1.ws ! WsMsg.In.Ping(23)
    player1.ws.expectWsMsgT[Out.Pong]()

    player1.createTable(tableId)
    player1.ws.expectWsMsgT[WsMsg.Out.Riichi.RiichiState]()

    player1.ws ! UserCmd.JoinAsPlayer(tableId, player1.user)
    player1.ws.expectWsMsgT[WsMsg.Out.Riichi.PlayerJoinedTable]()

    player2.ws ! UserCmd.JoinAsPlayer(tableId, player2.user)
    player1.ws.expectWsMsgT[WsMsg.Out.Riichi.PlayerJoinedTable]()

    player3.ws ! UserCmd.JoinAsPlayer(tableId, player3.user)
    player1.ws.expectWsMsgT[WsMsg.Out.Riichi.PlayerJoinedTable]()

    player4.ws ! UserCmd.JoinAsPlayer(tableId, player4.user)
    player1.ws.expectWsMsgT[WsMsg.Out.Riichi.PlayerJoinedTable]()

    player1.ws ! RiichiGameCmd.StartGame(tableId, gameId, RiichiConfig())
    player1.ws.expectWsMsgT[WsMsg.Out.Riichi.GameStarted]()

    player1.ws ! UserCmd.GetState(tableId, player1.userId)
    player1.ws.expectWsMsg {
      case state: WsMsg.Out.Riichi.RiichiState =>
        state.states.size should be (4)
        state
    }

    player2.ws ! UserCmd.GetState(tableId, player2.userId)
    player2.ws.expectWsMsg{
      case state: WsMsg.Out.Riichi.RiichiState =>
        state.states.size should be (4)
        state
    }
  }
}
