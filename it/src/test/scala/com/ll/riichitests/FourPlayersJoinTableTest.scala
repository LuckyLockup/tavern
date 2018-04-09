package com.ll.riichitests

import com.ll.domain.auth.UserId
import com.ll.domain.messages.WsMsg
import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.persistence.UserCmd
import com.ll.utils.{CommonData, Test}

class FourPlayersJoinTableTest extends Test {

  "Join game" in new CommonData {
    val player1 = createNewPlayer(UserId(101))
    val player2 = createNewPlayer(UserId(102))
    val player3 = createNewPlayer(UserId(103))
    val player4 = createNewPlayer(UserId(104))
    val player5 = createNewPlayer(UserId(105))

    player1.ws ! WsMsg.In.Ping(23)
    player1.ws.expectWsMsg[Out.Pong]

    player1.createTable(tableId)

    player1.ws ! UserCmd.JoinAsPlayer(tableId, player1.player)
    player1.ws.expectWsMsg[WsMsg.Out.Table.PlayerJoinedTable]

    player2.ws ! UserCmd.JoinAsPlayer(tableId, player2.player)
    player1.ws.expectWsMsg[WsMsg.Out.Table.PlayerJoinedTable]

    player3.ws ! UserCmd.JoinAsPlayer(tableId, player3.player)
    player1.ws.expectWsMsg[WsMsg.Out.Table.PlayerJoinedTable]

    player4.ws ! UserCmd.JoinAsPlayer(tableId, player4.player)
    player1.ws.expectWsMsg[WsMsg.Out.Table.PlayerJoinedTable]

    player5.ws ! UserCmd.JoinAsPlayer(tableId, player5.player)
    player5.ws.expectWsMsg[WsMsg.Out.ValidationError]
  }
}
