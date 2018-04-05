package com.ll.commontests

import com.ll.Test
import com.ll.domain.ws.WsMsg
import org.scalatest.FunSuite

class PingPongTest extends Test {

  test("ping pong") {
    val ws = createWsConnection(123)

    ws ! WsMsg.In.Ping(22)
    ws.expect {case msg @ WsMsg.Out.Pong(_) => msg }
    Thread.sleep(1000)
  }

}

