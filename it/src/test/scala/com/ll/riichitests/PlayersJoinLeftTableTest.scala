package com.ll.riichitests

import com.ll.domain.auth.UserId
import com.ll.domain.messages.WsMsg
import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.persistence.UserCmd
import com.ll.utils.{CommonData, Test}

class PlayersJoinLeftTableTest extends Test {

  "Join game" in new CommonData {
    val player1 = createNewPlayer(UserId(101))
    val player2 = createNewPlayer(UserId(102))
    val player3 = createNewPlayer(UserId(103))
    val player4 = createNewPlayer(UserId(104))
    val player5 = createNewPlayer(UserId(105))

    player1.ws ! WsMsg.In.Ping(23)
    player1.ws.expectWsMsg[Out.Pong]

    player1.createTable(tableId)

    player1.ws ! UserCmd.JoinAsPlayer(tableId, player1.user)
    player1.ws.expectWsMsg[WsMsg.Out.Table.PlayerJoinedTable]

    player2.ws ! UserCmd.JoinAsPlayer(tableId, player2.user)
    player1.ws.expectWsMsg[WsMsg.Out.Table.PlayerJoinedTable]

    player3.ws ! UserCmd.JoinAsPlayer(tableId, player3.user)
    player1.ws.expectWsMsg[WsMsg.Out.Table.PlayerJoinedTable]

    player4.ws ! UserCmd.JoinAsPlayer(tableId, player4.user)
    player1.ws.expectWsMsg[WsMsg.Out.Table.PlayerJoinedTable]

    player5.ws ! UserCmd.JoinAsPlayer(tableId, player5.user)
    player5.ws.expectWsMsg[WsMsg.Out.ValidationError]
  }

  "Join & left game" in new CommonData {
    val player1 = createNewPlayer(UserId(101))
    val player2 = createNewPlayer(UserId(102))

    player1.ws ! WsMsg.In.Ping(23)
    player1.ws.expectWsMsg[Out.Pong]

    player1.createTable(tableId)

    player1.ws ! UserCmd.GetState(tableId, player1.userId)
    player1.ws.expectWsMsg{
      case state: WsMsg.Out.Table.TableState =>
        state.players shouldBe empty
        state
    }

    player1.ws ! UserCmd.JoinAsPlayer(tableId, player1.user)
    player1.ws.expectWsMsg[WsMsg.Out.Table.PlayerJoinedTable]

    player1.ws ! UserCmd.GetState(tableId, player1.userId)
    player1.ws.expectWsMsg{
      case state: WsMsg.Out.Table.TableState if state.players.size == 1 => state
    }


    player2.ws ! UserCmd.JoinAsPlayer(tableId, player2.user)
    player1.ws.expectWsMsg[WsMsg.Out.Table.PlayerJoinedTable]

    player1.ws ! UserCmd.GetState(tableId, player1.userId)
    player1.ws.expectWsMsg{
      case state: WsMsg.Out.Table.TableState  =>
        state.players.size should be (2)
        state
    }

    player2.ws ! UserCmd.LeftAsPlayer(tableId, player2.user)
    player1.ws.expectWsMsg[WsMsg.Out.Table.PlayerLeftTable]

    player1.ws ! UserCmd.GetState(tableId, player1.userId)
    player1.ws.expectWsMsg{
      case state: WsMsg.Out.Table.TableState  =>
        state.players.size should be (1)
        state
    }

  }
}
