package com.ll.tests.riichitests.claiming

import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.riichi.{RiichiConfig, TestingState}
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.WsMsgOut
import com.ll.utils.{CommonData, Test}
import com.ll.utils.Test

class DeclareRonTest extends Test{
  "Declare ron test" in new CommonData {

    val st = TestingState(
      eastHand = List(
        "3_pin", "west", "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "green", "green", "green"
      ),
      southHand = List(
        "north", "north", "4_sou", "4_sou", "red", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou"
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
      case msg@WsMsgOut.Riichi.TileFromWallTaken(`tableId`, `gameId`, 2, RiichiPosition.SouthPosition, "3_pin", Nil) => msg
    }
    player2.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, "3_pin", 3)

    val declareRon = WsRiichiCmd.DeclareRon(tableId, gameId, 4, None)
    player1.ws.expectWsMsg {
      case msg@WsMsgOut.Riichi.TileDiscarded(`tableId`, `gameId`, 3, RiichiPosition.SouthPosition, "3_pin", cmds) =>
        cmds.size should be (1)
        cmds.head shouldBe a [WsRiichiCmd.DeclareRon]
        val declareRon = cmds.head.asInstanceOf[WsRiichiCmd.DeclareRon]
        declareRon.turn should be (4)
        msg
    }
    player1.ws ! WsRiichiCmd.DeclareRon(tableId, gameId, 4, None)

    player2.ws.expectWsMsg{
      case msg@WsMsgOut.Riichi.RonDeclared(`tableId`, `gameId`, 4, RiichiPosition.EastPosition) => msg
    }
    player2.ws.expectWsMsg{
      case msg: WsMsgOut.Riichi.GameScored => msg
    }
  }
}
