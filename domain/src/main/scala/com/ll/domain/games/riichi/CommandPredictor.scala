package com.ll.domain.games.riichi

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.deck.Tile
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.riichi.result.{HandValue, Tenpai}
import com.ll.domain.ws.WsMsgOut.ValidationError
import com.ll.domain.ops.EitherOps._
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd

object CommandPredictor {

  def predictsCommandsOnDiscard(table: GameStarted, discardedTile: Tile, discardedPosition: PlayerPosition[Riichi]):
  Map[PlayerPosition[Riichi], List[WsRiichiCmd]] = {
    table.playerStates
      //commands are predicted on player discard. The player who discarded can't take tile.
      .filter(st => st.player.position != discardedPosition)
      .map { st =>
        val declarePungs: Option[WsRiichiCmd.ClaimPung] = st.pungOn(discardedTile)
          .map(pung => WsRiichiCmd.ClaimPung(
            table.tableId,
            table.gameId,
            discardedPosition,
            table.turn + 1,
            List(pung.x.repr, pung.y.repr, pung.z.repr))
          )
        val declareChow = st.chowsOn(discardedTile, discardedPosition)
          .map(chow => WsRiichiCmd.ClaimPung(
            table.tableId,
            table.gameId,
            discardedPosition,
            table.turn + 1,
            List(chow.x.repr, chow.y.repr, chow.z.repr))
          )
        val declareRon = HandValue.computeRonOnTile(discardedTile, st).map(handValue =>
          WsRiichiCmd.DeclareRon(
            table.tableId,
            table.gameId,
            handValue)
        )
        st.player.position -> (declarePungs.toList ::: declareChow ::: declareRon.toList)
      }
      .filter{case (_, commands) => commands.nonEmpty}
      .toMap
  }

  def predictCommandsOnTileFromTheWall(table: GameStarted, tile: Tile, playerState: PlayerState): List[WsRiichiCmd] = {
    val tsumo = HandValue.computeTsumoOnTile(tile, playerState)
      .toList
      .map(v => WsRiichiCmd.DeclareTsumo(table.tableId, table.gameId, Some(v)))
    //TODO open kong
    //TODO declare riichi
    tsumo
  }

  def predictTsumo(tile: Tile, playerState: PlayerState, tableState: RiichiTableState): Option[HandValue] = {
    ???
  }

  def tenpai(state: PlayerState): Either[ValidationError, Tenpai] = {
    for {
      _ <- state.currentTile.isEmpty.asEither("Tenpai should be computed without current tile.")
    } yield ???
  }

  def getSets(tiles: List[Tile]) = {
    val distribution = TilesDistribution(tiles)

    distribution.pins

  }
}
