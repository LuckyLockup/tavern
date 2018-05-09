package com.ll.tests.riichitests

import com.ll.domain.auth.UserId
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.riichi.{RiichiConfig, TestingState}
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.WsMsgOut
import com.ll.utils.{CommonData, Test}

class DeclarePungTest extends Test {
  "Declare pung test" in new CommonData {
    val player1 = createNewPlayer()
    val player2 = createNewPlayer()

    player1.createTable(tableId)
    player1.joinTable(tableId)
    player2.joinTable(tableId)

    val st = TestingState(
      eastHand = List(
        "red", "3_pin", "3_pin", "4_pin", "4_pin", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou"
      ),
      southHand = List(
        "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
      ),
      westHand = List(
        "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
      ),
      northHand = List(
        "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
      ),
      uraDoras = List("2_sou"),
      wall = List("3_pin")
    )

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(testingTiles = Some(st))))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, st.eastHand.head, 1)
    player2.ws.expectWsMsg {
      case discard: WsMsgOut.Riichi.TileDiscarded =>
        discard.position should be(PlayerPosition.RiichiPosition.EastPosition)
        discard.tile should be (st.eastHand.head)
        discard
    }

    player2.ws.expectWsMsg {
      case fromTheWall: WsMsgOut.Riichi.TileFromWallTaken =>
        fromTheWall.position should be(RiichiPosition.SouthPosition)
        fromTheWall.tile should be(st.wall(1))
        fromTheWall
    }
    player2.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, st.wall(1), 3)
    val discard = player1.ws.expectWsMsg {
      case discard: WsMsgOut.Riichi.TileDiscarded if discard.tile == st.wall(1) =>
        discard.position should be(PlayerPosition.RiichiPosition.SouthPosition)
        discard
    }

    val declarePungOpt = discard.commands.collectFirst{case c: WsRiichiCmd.ClaimPung => c}
    declarePungOpt should not be empty

    player1.ws ! declarePungOpt.get
    player1.ws.expectWsMsg {
      case claimed: WsMsgOut.Riichi.TileClaimed =>
        claimed
    }
  }
}
