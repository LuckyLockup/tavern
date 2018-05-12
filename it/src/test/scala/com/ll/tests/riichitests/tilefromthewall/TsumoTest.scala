package com.ll.tests.riichitests.tilefromthewall

import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.riichi.result.Points
import com.ll.domain.games.riichi.{RiichiConfig, TestingState}
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.WsMsgOut
import com.ll.utils.{CommonData, Test}

class TsumoTest extends Test {
  "Declaring tsumo" in new CommonData {
    val st = TestingState(
      eastHand = List("west", "north", "2_wan", "7_wan", "7_wan", "red", "green", "1_sou", "8_sou", "1_pin"),
      southHand = List(
        "2_pin", "3_pin", "3_pin", "4_pin", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou"
      ),
      wall = List("3_pin")
    )

    val player1 = createNewPlayer()
    val player2 = createNewPlayer()

    player1.createTable(tableId)
    player1.joinTable(tableId)
    player2.joinTable(tableId)

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(testingTiles = Some(st))))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, "west", 1)
    player2.ws.expectWsMsg {
      case discard: WsMsgOut.Riichi.TileDiscarded =>
        discard.position should be(PlayerPosition.RiichiPosition.EastPosition)
        discard.tile should be ("west")
        discard.commands shouldBe empty
        discard
    }
    player2.ws.expectWsMsg{
      case msg@WsMsgOut.Riichi.TileFromWallTaken(`tableId`, `gameId`, 2, RiichiPosition.SouthPosition, "3_pin", cmds) =>
        cmds.size shouldBe (1)
        cmds.head shouldBe a [WsRiichiCmd.DeclareTsumo]
        val declareTsumo = cmds.head.asInstanceOf[WsRiichiCmd.DeclareTsumo]
        declareTsumo.turn should be (3)
        msg
    }
    player2.ws ! WsRiichiCmd.DeclareTsumo(tableId, gameId, 3, None)

    player1.ws.expectWsMsg {
      case tsumo: WsMsgOut.Riichi.TsumoDeclared =>
        tsumo.position should be(RiichiPosition.SouthPosition)
        tsumo.turn should be(3)
        tsumo
    }
    val score = player1.ws.expectWsMsg {
      case score: WsMsgOut.Riichi.GameScored => score
    }
  }
}
