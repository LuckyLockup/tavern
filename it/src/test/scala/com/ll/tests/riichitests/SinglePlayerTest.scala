package com.ll.tests.riichitests

import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.WsMsgOut
import com.ll.utils.{CommonData, Test}

class SinglePlayerTest extends Test {
  "Start game with 1 player" in new CommonData {
    val player1 = createNewPlayer()

    player1.createTable(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.RiichiState]()

    player1.ws ! WsRiichiCmd.JoinAsPlayer(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.PlayerJoinedTable]()

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    player1.ws ! WsRiichiCmd.GetState(tableId)
    val state1: WsMsgOut.Riichi.RiichiState = player1.ws.expectWsMsg {
      case state: WsMsgOut.Riichi.RiichiState =>
        state.states.size should be(4)
        state.turn should be(1)
        state
    }

    player1.ws.expectWsMsg {
      case taken: WsMsgOut.Riichi.TileFromWallTaken =>
        taken.position should be (RiichiPosition.EastPosition)
        taken
    }

    val tileToDiscard = state1.states.head.closedHand.head
    player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, tileToDiscard, 2)
    player1.ws.expectWsMsg {
      case discarded: WsMsgOut.Riichi.TileDiscarded =>
        discarded.tile should be(tileToDiscard)
        discarded.turn should be(2)
        discarded
    }

    player1.ws ! WsRiichiCmd.GetState(tableId)
    player1.ws.expectWsMsg {
      case state: WsMsgOut.Riichi.RiichiState =>
        state.states.size should be(4)
        val east = state.states.head
        state.turn should be(3)
        east.closedHand.size should be(13)
        east.currentTile should be(None)
        east.discard should be(List(tileToDiscard))
        state
    }
  }

  "Basic game play" in new CommonData {
    val player1 = createNewPlayer()

    player1.createTable(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.RiichiState]()

    player1.ws ! WsRiichiCmd.JoinAsPlayer(tableId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.PlayerJoinedTable]()

    player1.ws ! WsRiichiCmd.StartWsGame(tableId, gameId)
    player1.ws.expectWsMsgT[WsMsgOut.Riichi.GameStarted]()

    for {round <- 0 to 10} {
      val tileToDiscard =  player1.ws.expectWsMsg {
        case taken: WsMsgOut.Riichi.TileFromWallTaken =>
          taken.position should be (RiichiPosition.EastPosition)
          taken
      }
      player1.ws ! WsRiichiCmd.DiscardTile(tableId, gameId, tileToDiscard.tile, 2 + 8 *round)
      player1.ws.expectWsMsg {
        case discarded: WsMsgOut.Riichi.TileDiscarded =>
          discarded.tile should be(tileToDiscard.tile)
          discarded
      }

      List(RiichiPosition.SouthPosition, RiichiPosition.WestPosition, RiichiPosition.NorthPosition).foreach { pos =>
        player1.ws.expectWsMsg {
          case tileTaken: WsMsgOut.Riichi.TileFromWallTaken =>
            tileTaken.position should be(pos)
            tileTaken
        }

        val discared = player1.ws.expectWsMsg {
          case tileDiscarded: WsMsgOut.Riichi.TileDiscarded =>
            tileDiscarded.position should be(pos)
            tileDiscarded
        }
        discared.commands.map{
          case cmd: WsRiichiCmd.ClaimChow => cmd.turn
          case cmd: WsRiichiCmd.ClaimPung => cmd.turn
//          case cmd: WsRiichiCmd.DeclareRon => cmd.turn
        }.foreach {turn =>
          player1.ws ! WsRiichiCmd.SkipAction(tableId, gameId, turn)
          player1.ws.expectWsMsg {
            case skipped: WsMsgOut.Riichi.ActionSkipped =>
              skipped.turn should be (turn)
              skipped
          }
        }
      }
    }
  }
}
