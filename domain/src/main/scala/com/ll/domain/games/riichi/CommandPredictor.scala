package com.ll.domain.games.riichi

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.deck.Tile
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.riichi.result.{HandValue, Tenpai}
import com.ll.domain.ws.WsMsgOut.ValidationError
import com.ll.domain.ops.EitherOps._
import com.ll.domain.persistence.TableCmd.RiichiCmd

object CommandPredictor {

  def predictsCommandsOnDiscard(table: GameStarted, discardedTile: Tile, discardedPosition: PlayerPosition[Riichi]):
  Map[PlayerPosition[Riichi], List[RiichiCmd]] = {
    table.playerStates
      //commands are predicted on player discard. The player who discarded can't take tile.
      .filter(st => st.player.position != discardedPosition)
      .map { st =>
        val declarePungs: Option[RiichiCmd.ClaimPung] = st.pungOn(discardedTile)
          .map(pung => RiichiCmd.ClaimPung(
            table.tableId,
            table.gameId,
            discardedPosition,
            table.turn + 1,
            List(pung.x.repr, pung.y.repr, pung.z.repr),
            st.player.position)
          )
        val declareChow = st.chowsOn(discardedTile, discardedPosition)
          .map(chow => RiichiCmd.ClaimPung(
            table.tableId,
            table.gameId,
            discardedPosition,
            table.turn + 1,
            List(chow.x.repr, chow.y.repr, chow.z.repr),
            st.player.position)
          )
        val declareRon = HandValue.computeRonOnTile(discardedTile, st).map(handValue =>
          RiichiCmd.DeclareRon(
            table.tableId,
            table.gameId,
            handValue,
            st.player.position)
        )
        st.player.position -> (declarePungs.toList ::: declareChow ::: declareRon.toList)
      }
      .toMap
  }

  def predictCommandsOnTileFromTheWall(table: GameStarted, tile: Tile, playerState: PlayerState): List[RiichiCmd] = {
    val tsumo = HandValue.computeTsumoOnTile(tile, playerState)
      .toList
      .map(v => RiichiCmd.DeclareTsumo(table.tableId, table.gameId, Some(v), playerState.player.position))
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
