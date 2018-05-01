package com.ll.riichitests

import com.ll.domain.auth.UserId
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.riichi.RiichiConfig
import com.ll.domain.ws.WsMsgIn.{RiichiGameCmd, UserCmd}
import com.ll.domain.ws.WsMsgOut
import com.ll.utils.{CommonData, Test}

class DeclarePungTest extends Test {
  "Declare pung test" in new CommonData {
    val player1 = createNewPlayer(UserId(101))
    val player2 = createNewPlayer(UserId(102))

    player1.createTable(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.RiichiState]()

    player1.ws ! UserCmd.JoinAsPlayer(tableId, player1.user)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.PlayerJoinedTable]()

    player2.ws ! UserCmd.JoinAsPlayer(tableId, player2.user)
    player2.ws.expectWsMsgT[WsMsgOut.Riichi.PlayerJoinedTable]()

    val eastHand = List(
      "3_pin", "3_pin", "4_pin", "4_pin", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou"
    )
    val southHand = List(
      "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
    )
    val westHand = List(
      "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
    )
    val northHand = List(
      "1_wan", "2_wan", "3_wan", "4_wan", "5_wan", "6_wan", "7_wan", "8_wan", "9_wan", "5_sou", "6_sou", "7_sou", "8_sou"
    )
    val uraDoras = List("2_sou")
    val wallTiles = List("red", "3_pin")

    player1.ws ! RiichiGameCmd.StartGame(tableId, gameId, RiichiConfig().copy(testingTiles =
      eastHand ::: southHand ::: westHand ::: northHand ::: uraDoras ::: wallTiles
    ))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws.expectWsMsg {
      case fromTheWall: WsMsgOut.Riichi.TileFromWallTaken =>
        fromTheWall.position should be(RiichiPosition.EastPosition)
        fromTheWall.tile should be(wallTiles.head)
        fromTheWall
    }

    player1.ws ! RiichiGameCmd.DiscardTile(tableId, gameId, wallTiles.head, 2)
    player2.ws.expectWsMsg {
      case discard: WsMsgOut.Riichi.TileDiscarded =>
        discard.position should be(PlayerPosition.RiichiPosition.EastPosition)
        discard.tile should be (wallTiles.head)
        discard
    }

    player2.ws.expectWsMsg {
      case fromTheWall: WsMsgOut.Riichi.TileFromWallTaken =>
        fromTheWall.position should be(RiichiPosition.SouthPosition)
        fromTheWall.tile should be(wallTiles(1))
        fromTheWall
    }
    player2.ws ! RiichiGameCmd.DiscardTile(tableId, gameId, wallTiles(1), 4)
    val discard = player1.ws.expectWsMsg {
      case discard: WsMsgOut.Riichi.TileDiscarded if discard.tile == wallTiles(1) =>
        discard.position should be(PlayerPosition.RiichiPosition.SouthPosition)
        discard
    }

    val declarePungOpt = discard.commands.collectFirst{case c: RiichiGameCmd.ClaimPung => c}
    declarePungOpt should not be empty
    declarePungOpt.get.tiles should contain theSameElementsAs List("3_pin",  "3_pin",  "3_pin")

    player1.ws ! declarePungOpt.get
    player1.ws.expectWsMsg {
      case claimed: WsMsgOut.Riichi.TileClaimed =>
        claimed
    }
  }
}
