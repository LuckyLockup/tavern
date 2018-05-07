package com.ll.domain.games.riichi

import com.ll.domain.games.{GameId, TableId}
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.deck.Tile
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.riichi.result.{HandValue, Tenpai}
import com.ll.domain.ws.WsMsgOut.ValidationError
import com.ll.domain.ops.EitherOps._
import com.ll.domain.persistence.TableCmd.RiichiCmd
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd

object CommandPredictor {

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
