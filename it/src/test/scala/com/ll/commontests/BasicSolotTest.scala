package com.ll.commontests

import com.ll.domain.ws.WsMsg
import com.ll.domain.ws.WsMsg.Out
import com.ll.utils.{CommonData, Test}

class BasicSolotTest extends Test {

  "Join game" in new CommonData {
    val player = createNewPlayer(userId)

    player.ws ! WsMsg.In.Ping(23)
    player.ws.expectWsMsg[Out.Pong]

    player.createGame(gameId)

    //TODO proper game creation
    Thread.sleep(1000)

    player.ws ! WsMsg.In.JoinGameAsPlayer(gameId)

    player.ws.expectWsMsg[WsMsg.Out.GameState]
    player.ws.expectWsMsg[WsMsg.Out.TileFromWall]

  }
}
