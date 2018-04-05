package com.ll.commontests

import com.ll.domain.ws.WsMsg
import com.ll.utils.{CommonData, Test}

class PingPongTest extends Test {

  "Ping Pong" in new CommonData {
    val player = createNewPlayer(userId)

    player.ws ! WsMsg.In.Ping(22)
    player.ws.expectWsMsg[WsMsg.Out.Pong]
  }
}

