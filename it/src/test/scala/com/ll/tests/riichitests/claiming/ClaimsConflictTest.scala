package com.ll.tests.riichitests.claiming

import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.riichi.{RiichiConfig, TestingState}
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.WsMsgOut
import com.ll.utils.{CommonData, Test}

import com.ll.utils.Test
import scala.concurrent.duration._

class ClaimsConflictTest extends Test {
  val st = TestingState(
    eastHand = List(
      "5_wan", "red", "1_pin", "2_pin", "3_pin", "4_pin", "5_pin", "6_pin", "7_pin", "8_pin", "9_pin", "north", "north", "north"
    ),
    southHand = List(
      "4_wan", "6_wan", "1_pin", "2_pin", "3_pin", "4_pin", "5_pin", "6_pin", "7_pin", "8_pin", "9_pin", "south", "green"
    ),
    westHand = List(
      "5_wan", "5_wan","1_pin", "2_pin", "3_pin", "4_pin", "5_pin", "6_pin", "7_pin", "8_pin", "9_pin", "south", "green"
    ),
    northHand = List(
      "5_wan", "1_pin", "2_pin", "3_pin", "4_pin", "5_pin", "6_pin", "7_pin", "8_pin", "9_pin", "white", "white", "white"
    )
  )

  "All commands are received" in new CommonData {
    val player1 = createNewPlayer()
    val player2 = createNewPlayer()
    val player3 = createNewPlayer()
    val player4 = createNewPlayer()

    player1.createTable(tableId)
    player1.createTable(tableId)
    player1.joinTable(tableId)
    player2.joinTable(tableId)
    player3.joinTable(tableId)
    player4.joinTable(tableId)

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(testingTiles = Some(st))))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, "5_wan", 1)

    val claimChow = WsRiichiCmd.ClaimChow(tableId, gameId, 2, "5_wan", List("4_wan", "6_wan"))
    player2.ws.expectWsMsg {
      case discard: WsMsgOut.Riichi.TileDiscarded =>
        discard.position should be(PlayerPosition.RiichiPosition.EastPosition)
        discard.tile should be ("5_wan")
        discard.commands should contain theSameElementsAs List(claimChow)
        discard
    }

    val claimPung = WsRiichiCmd.ClaimPung(tableId, gameId, 2)
    player3.ws.expectWsMsg {
      case discard: WsMsgOut.Riichi.TileDiscarded =>
        discard.position should be(PlayerPosition.RiichiPosition.EastPosition)
        discard.tile should be ("5_wan")
        discard.commands should contain theSameElementsAs List(claimPung)
        discard
    }

    val declareRon = WsRiichiCmd.DeclareRon(tableId, gameId, 2, None)
    player4.ws.expectWsMsg {
      case msg@WsMsgOut.Riichi.TileDiscarded(`tableId`, `gameId`, 1, RiichiPosition.EastPosition, "5_wan", cmds) =>
        cmds.size should be (1)
        cmds.head shouldBe a [WsRiichiCmd.DeclareRon]
        val declareRon = cmds.head.asInstanceOf[WsRiichiCmd.DeclareRon]
        declareRon.turn should be (2)
        msg
    }
  }

  "Ron declared immediately" in new CommonData {
    val player1 = createNewPlayer()
    val player2 = createNewPlayer()
    val player3 = createNewPlayer()
    val player4 = createNewPlayer()

    player1.createTable(tableId)
    player1.createTable(tableId)
    player1.joinTable(tableId)
    player2.joinTable(tableId)
    player3.joinTable(tableId)
    player4.joinTable(tableId)

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(
      nextTileDelay = 30.seconds,
      testingTiles = Some(st))))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, "5_wan", 1)
    player1.ws.expectWsMsg{
      case msg: WsMsgOut.Riichi.TileDiscarded => msg
    }

    player4.ws !  WsRiichiCmd.DeclareRon(tableId, gameId, 2, None)
    player1.ws.expectWsMsg{
      case msg: WsMsgOut.Riichi.RonDeclared => msg
    }
  }

  "Chow declared if other options are skipped" in new CommonData {
    val player1 = createNewPlayer()
    val player2 = createNewPlayer()
    val player3 = createNewPlayer()
    val player4 = createNewPlayer()

    player1.createTable(tableId)
    player1.createTable(tableId)
    player1.joinTable(tableId)
    player2.joinTable(tableId)
    player3.joinTable(tableId)
    player4.joinTable(tableId)

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(
      nextTileDelay = 30.seconds,
      testingTiles = Some(st))))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, "5_wan", 1)
    player1.ws.expectWsMsg{
      case msg: WsMsgOut.Riichi.TileDiscarded => msg
    }

    player2.ws ! WsRiichiCmd.ClaimChow(tableId, gameId, 2, "5_wan", List("4_wan", "6_wan"))
    player3.skipAction(tableId, gameId, 2)
    player4.skipAction(tableId, gameId, 2)
    player1.ws.expectWsMsg{
      case msg: WsMsgOut.Riichi.TileClaimed =>
        msg.setName shouldBe ("chow")
        msg
    }
  }

  "Pong declared if ron is skipped" in new CommonData {
    val player1 = createNewPlayer()
    val player2 = createNewPlayer()
    val player3 = createNewPlayer()
    val player4 = createNewPlayer()

    player1.createTable(tableId)
    player1.createTable(tableId)
    player1.joinTable(tableId)
    player2.joinTable(tableId)
    player3.joinTable(tableId)
    player4.joinTable(tableId)

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(
      nextTileDelay = 30.seconds,
      testingTiles = Some(st))))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, "5_wan", 1)
    player1.ws.expectWsMsg{
      case msg: WsMsgOut.Riichi.TileDiscarded => msg
    }

    player3.ws ! WsRiichiCmd.ClaimPung(tableId, gameId, 2)
    player4.skipAction(tableId, gameId, 2)
    player1.ws.expectWsMsg{
      case msg: WsMsgOut.Riichi.TileClaimed =>
        msg.setName shouldBe ("pung")
        msg
    }
  }

  "Between pung and chow, pung is chosen" in new CommonData {
    val player1 = createNewPlayer()
    val player2 = createNewPlayer()
    val player3 = createNewPlayer()
    val player4 = createNewPlayer()

    player1.createTable(tableId)
    player1.createTable(tableId)
    player1.joinTable(tableId)
    player2.joinTable(tableId)
    player3.joinTable(tableId)
    player4.joinTable(tableId)

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(
      nextTileDelay = 30.seconds,
      testingTiles = Some(st))))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, "5_wan", 1)
    player1.ws.expectWsMsg{
      case msg: WsMsgOut.Riichi.TileDiscarded => msg
    }

    player2.ws ! WsRiichiCmd.ClaimChow(tableId, gameId, 2, "5_wan", List("4_wan", "6_wan"))
    player3.ws ! WsRiichiCmd.ClaimPung(tableId, gameId, 2)
    player4.skipAction(tableId, gameId, 2)
    player1.ws.expectWsMsg{
      case msg: WsMsgOut.Riichi.TileClaimed =>
        msg.setName shouldBe ("pung")
        msg
    }
  }

  "Between pung, ron and  chow, ron is chosen" in new CommonData {
    val player1 = createNewPlayer()
    val player2 = createNewPlayer()
    val player3 = createNewPlayer()
    val player4 = createNewPlayer()

    player1.createTable(tableId)
    player1.createTable(tableId)
    player1.joinTable(tableId)
    player2.joinTable(tableId)
    player3.joinTable(tableId)
    player4.joinTable(tableId)

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(
      nextTileDelay = 30.seconds,
      testingTiles = Some(st))))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, "5_wan", 1)
    player1.ws.expectWsMsg{
      case msg: WsMsgOut.Riichi.TileDiscarded => msg
    }

    player2.ws ! WsRiichiCmd.ClaimChow(tableId, gameId, 2, "5_wan", List("4_wan", "6_wan"))
    player3.ws ! WsRiichiCmd.ClaimPung(tableId, gameId, 2)
    player4.ws ! WsRiichiCmd.DeclareRon(tableId, gameId, 2, None)
    player1.ws.expectWsMsg{
      case msg: WsMsgOut.Riichi.RonDeclared =>
        msg
    }
  }

  "If all actions skipped, then next tile is chosen" in new CommonData {
    val player1 = createNewPlayer()
    val player2 = createNewPlayer()
    val player3 = createNewPlayer()
    val player4 = createNewPlayer()

    player1.createTable(tableId)
    player1.createTable(tableId)
    player1.joinTable(tableId)
    player2.joinTable(tableId)
    player3.joinTable(tableId)
    player4.joinTable(tableId)

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(
      nextTileDelay = 30.seconds,
      testingTiles = Some(st))))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, "5_wan", 1)
    player1.ws.expectWsMsg{
      case msg: WsMsgOut.Riichi.TileDiscarded => msg
    }

    player4.skipAction(tableId, gameId, 2)
    player2.skipAction(tableId, gameId, 2)
    player3.skipAction(tableId, gameId, 2)
    player1.ws.expectWsMsg{
      case msg: WsMsgOut.Riichi.TileFromWallTaken =>
        msg.position shouldBe RiichiPosition.SouthPosition
        msg
    }
  }

}
