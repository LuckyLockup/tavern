package com.ll.riichitests

import com.ll.domain.auth.UserId
import com.ll.domain.games.riichi.RiichiConfig
import com.ll.domain.messages.WsMsg
import com.ll.domain.persistence.{RiichiGameCmd, UserCmd}
import com.ll.utils.{CommonData, Test}

class SinglePlayerTest extends Test{
  "Start game with 1 player" in new CommonData {
    val player1 = createNewPlayer(UserId(101))

    player1.createTable(tableId)
    player1.ws.expectWsMsgT[WsMsg.Out.Riichi.RiichiState]()

    player1.ws ! UserCmd.JoinAsPlayer(tableId, player1.user)
    player1.ws.expectWsMsgT[WsMsg.Out.Riichi.PlayerJoinedTable]()


    player1.ws ! RiichiGameCmd.StartGame(tableId, gameId, RiichiConfig())
    player1.ws.expectWsMsgT[WsMsg.Out.Riichi.GameStarted]()

    player1.ws ! UserCmd.GetState(tableId, player1.userId)
    val state1: WsMsg.Out.Riichi.RiichiState = player1.ws.expectWsMsg {
      case state: WsMsg.Out.Riichi.RiichiState =>
        state.states.size should be (4)
        state.turn should be (1)
        state
    }

    val tileToDiscard = state1.states.head.closedHand.head
    player1.ws ! RiichiGameCmd.DiscardTile(tableId, gameId, tileToDiscard, 1)
    player1.ws.expectWsMsg {
      case discarded: WsMsg.Out.Riichi.TileDiscarded =>
        discarded.tile should be (tileToDiscard)
        discarded.turn should be (1)
        discarded
    }

    player1.ws ! UserCmd.GetState(tableId, player1.userId)
    player1.ws.expectWsMsg {
      case state: WsMsg.Out.Riichi.RiichiState =>
        state.states.size should be (4)
        val east = state.states.head
        state.turn should be (2)
        east.closedHand.size should be (13)
        east.currentTile should be (None)
        east.discard should be (List(tileToDiscard))
        state
    }
  }
}
