package com.ll.tests.riichitests.claiming


import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.riichi.{RiichiConfig, TestingState}
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.WsMsgOut
import com.ll.domain.ws.WsRiichi.WsDeclaredSet
import com.ll.utils.{CommonData, Test}

import com.ll.utils.Test

class DeclareChowTest extends Test {
  "Declare chow test" in new CommonData {

    val st = TestingState(
      eastHand = List(
        "3_pin", "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
      ),
      southHand = List(
        "4_pin", "5_pin", "red", "red", "red", "1_sou", "2_sou", "3_sou",  "1_sou", "2_sou", "3_sou", "2_sou", "4_sou"
      ),
      uraDoras = List("2_sou"),
      wall = List("east", "west", "north")
    )

    val player1 = createNewPlayer()
    val player2 = createNewPlayer()
    val player3 = createNewPlayer()

    player1.createTable(tableId)
    player1.joinTable(tableId)
    player2.joinTable(tableId)
    player3.joinTable(tableId)

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(testingTiles = Some(st))))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, "3_pin", 1)
    val claimChow = WsRiichiCmd.ClaimChow(tableId, gameId, 2, "3_pin", List("4_pin", "5_pin"))
    player2.ws.expectWsMsg {
      case discard: WsMsgOut.Riichi.TileDiscarded =>
        discard.position should be(PlayerPosition.RiichiPosition.EastPosition)
        discard.tile should be ("3_pin")
        discard.commands should contain theSameElementsAs List(claimChow)
        discard
    }

    player2.ws ! claimChow
    player3.ws.expectWsMsg {
      case msg@WsMsgOut.Riichi.TileClaimed(`tableId`, `gameId`, "chow",
      WsDeclaredSet("3_pin", List("4_pin", "5_pin"), RiichiPosition.EastPosition, 2),
      RiichiPosition.SouthPosition) => msg
    }

    player2.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, "red", 3)
    player3.ws.expectWsMsg {
      case msg@WsMsgOut.Riichi.TileDiscarded(`tableId`, `gameId`, 3, RiichiPosition.SouthPosition, "red", Nil) => msg
    }
    player3.ws.expectWsMsg{
      case msg@WsMsgOut.Riichi.TileFromWallTaken(`tableId`, `gameId`, 4, RiichiPosition.WestPosition, "east", Nil) => msg
    }

    player3.ws ! WsRiichiCmd.GetState(tableId)
    player3.ws.expectWsMsg {
      case state: WsMsgOut.Riichi.RiichiState =>
        state.states.foreach{
          case pst if pst.player.position == RiichiPosition.EastPosition =>
            pst.closedHand should contain theSameElementsAs Array.fill(13)("x")
            pst.discard shouldBe empty
            pst.openHand shouldBe empty
          case pst if pst.player.position == RiichiPosition.SouthPosition =>
            pst.closedHand should contain theSameElementsAs Array.fill(10)("x")
            pst.discard shouldBe List("red")
            pst.openHand shouldBe List(WsDeclaredSet("3_pin", List("4_pin", "5_pin"), RiichiPosition.EastPosition, 2))
          case pst =>
            pst.discard shouldBe empty
            pst.openHand shouldBe empty
        }
        state
    }
  }

  "Can't claim chow from wrong position" in new CommonData {

    val st = TestingState(
      eastHand = List(
        "4_pin", "5_pin", "red", "3_pin", "3_pin", "1_sou", "2_sou", "3_sou",  "1_sou", "2_sou", "3_sou", "2_sou", "4_sou","8_sou"
      ),
      southHand = List(
        "3_pin", "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou"
      ),
      wall = List("east", "west", "north")
    )

    val player1 = createNewPlayer()
    val player2 = createNewPlayer()

    player1.createTable(tableId)
    player1.joinTable(tableId)
    player2.joinTable(tableId)

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId, Some(RiichiConfig().copy(testingTiles = Some(st))))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId,"red", 1)
    player2.ws.expectWsMsg {
      case discard: WsMsgOut.Riichi.TileDiscarded =>
        discard.position should be(PlayerPosition.RiichiPosition.EastPosition)
        discard.tile should be ("red")
        discard.commands shouldBe empty
        discard
    }

    player2.ws.expectWsMsg{
      case msg@WsMsgOut.Riichi.TileFromWallTaken(`tableId`, `gameId`, 2, RiichiPosition.SouthPosition, "east", Nil) => msg
    }

    player2.ws ! WsRiichiCmd.DiscardTile(tableId, gameId,  "3_pin", 3)
    player1.ws.expectWsMsg {
      case msg@WsMsgOut.Riichi.TileDiscarded(`tableId`, `gameId`, 3, RiichiPosition.SouthPosition, "3_pin", cmds) =>
        cmds should contain theSameElementsAs List(WsRiichiCmd.ClaimPung(tableId, gameId, 4))
        msg
    }

    player1.ws !  WsRiichiCmd.ClaimChow(tableId, gameId, 4, "3_pin", List("4_pin", "5_pin"))
    player1.ws.expectWsMsg{
      case msg @ WsMsgOut.ValidationError("You can only take Chow from the previous player") => msg
    }
  }
}
