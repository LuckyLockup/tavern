package com.ll.tests.commontests

import com.ll.domain.games.TableId
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.{WsMsgIn, WsMsgOut}
import com.ll.utils.{CommonData, Test}

class ErrorsTest extends Test {
  "Get state for not existing table" in new CommonData {
    val player = createNewPlayer()

    player.ws ! WsMsgIn.Ping(22)
    player.ws.expectWsMsgT[WsMsgOut.Pong]()

    player.ws ! WsRiichiCmd.GetState(TableId("wrong_table_id"))
    val state1: WsMsgOut.ValidationError = player.ws.expectWsMsg {
      case state: WsMsgOut.ValidationError =>
        state
    }
  }
}
