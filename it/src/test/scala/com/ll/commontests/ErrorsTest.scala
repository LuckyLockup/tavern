package com.ll.commontests

import com.ll.domain.games.TableId
import com.ll.domain.ws.WsMsgIn.UserCmd
import com.ll.domain.ws.{WsMsgIn, WsMsgOut}
import com.ll.utils.{CommonData, Test}

class ErrorsTest extends Test {
  "Get state for not existing table" in new CommonData {
    val player = createNewPlayer(userId)

    player.ws ! WsMsgIn.Ping(22)
    player.ws.expectWsMsgT[WsMsgOut.Pong]()

    player.ws ! UserCmd.GetState(TableId("wrong_table_id"), player.userId)
    val state1: WsMsgOut.ValidationError = player.ws.expectWsMsg {
      case state: WsMsgOut.ValidationError =>
        state
    }
  }
}
