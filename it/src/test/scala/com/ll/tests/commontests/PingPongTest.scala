package com.ll.tests.commontests

import com.ll.domain.ws.{WsMsgIn, WsMsgOut}
import com.ll.utils.{CommonData, Test}

class PingPongTest extends Test {

  "Ping Pong" in new CommonData {
    val player = createNewPlayer()

    player.ws ! WsMsgIn.Ping(22)
    player.ws.expectWsMsgT[WsMsgOut.Pong]()
  }
}

