package com.ll.riichitests

import com.ll.domain.auth.UserId
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.riichi.RiichiConfig
import com.ll.domain.games.riichi.result.HandValue
import com.ll.domain.ws.WsMsgIn.{RiichiGameCmd, UserCmd}
import com.ll.domain.ws.WsMsgOut
import com.ll.utils.{CommonData, Test}

class TsumoTest extends Test {
  "Declaring tsumo" in new CommonData {
    val player1 = createNewPlayer(UserId(101))

    player1.createTable(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.RiichiState]()

    player1.ws ! UserCmd.JoinAsPlayer(tableId, player1.user)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.PlayerJoinedTable]()
    val eastHand = List(
      "2_pin", "3_pin", "3_pin", "4_pin", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou", "1_sou", "2_sou", "3_sou"
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
    val wallTiles = List("3_pin")

    player1.ws ! RiichiGameCmd.StartGame(tableId, gameId, RiichiConfig().copy(testingTiles =
      eastHand ::: southHand ::: westHand ::: northHand ::: uraDoras ::: wallTiles
    ))
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! UserCmd.GetState(tableId, player1.userId)
    val state1: WsMsgOut.Riichi.RiichiState = player1.ws.expectWsMsg {
      case state: WsMsgOut.Riichi.RiichiState =>
        state.states.size should be(4)
        state.uraDoras should contain theSameElementsAs uraDoras
        state.states.head.closedHand should contain theSameElementsAs eastHand
        state
    }
    val tileFromTheWall =  player1.ws.expectWsMsg {
      case fromTheWall: WsMsgOut.Riichi.TileFromWallTaken =>
        fromTheWall.position should be (RiichiPosition.EastPosition)
        fromTheWall.tile should be (wallTiles.head)
        fromTheWall.commands.head should be (RiichiGameCmd.DeclareTsumo(tableId, gameId, Some(HandValue(1, 1))))
        fromTheWall
    }
    player1.ws ! tileFromTheWall.commands.head.asInstanceOf[RiichiGameCmd.DeclareTsumo].copy(approxHandValue = None)

    player1.ws.expectWsMsg {
      case tsumo: WsMsgOut.Riichi.TsumoDeclared =>
        tsumo.position should be (RiichiPosition.EastPosition)
        tsumo.turn should be (2)
        tsumo
    }
    player1.ws.expectWsMsg {
      case score: WsMsgOut.Riichi.GameScored =>
        score
    }
  }
}
