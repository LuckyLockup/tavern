package com.ll.commontests

import com.ll.domain.messages.WsMsg
import com.ll.domain.messages.WsMsg.Out
import com.ll.domain.persistence.{TableCmd, UserCmd}
import com.ll.utils.{CommonData, Test}

class BasicSolotTest extends Test {

  "Join game" in new CommonData {
    val player = createNewPlayer(userId)

    player.ws ! WsMsg.In.Ping(23)
    player.ws.expectWsMsg[Out.Pong]

    player.createTable(tableId)

    //TODO proper game creation
    Thread.sleep(1000)

    player.ws ! UserCmd.JoinAsPlayer(tableId, user)
    player.ws ! TableCmd.StartGame(tableId)


    player.ws.expectWsMsg[WsMsg.Out.Table.TableState]
    player.ws.expectWsMsg[WsMsg.Out.Table.GameStarted]

  }
}
