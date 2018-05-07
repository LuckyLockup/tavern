package com.ll.domain.games.riichi.result

import com.ll.domain.games.deck.Tile
import com.ll.domain.games.riichi.{PlayerState, TileSetsHelper}
import com.ll.domain.json.CaseClassCodec
import io.circe.{Decoder, Encoder}

case class HandValue(
  miniPoints: Int,
  yakus: Int
)

object HandValue extends CaseClassCodec {
  implicit lazy val PauseGameEncoder: Encoder[HandValue] = encoder[HandValue]("HandValue")
  implicit lazy val PauseGameDecoder: Decoder[HandValue] = decoder[HandValue]("HandValue")

  def computeRonOnTile(tile: Tile, state: PlayerState): Option[HandValue] = {
    if (state.discard.exists(t => t.repr == tile.repr)) {
      //Player can't ron on discarded tile
      None
    } else {
      TileSetsHelper
        .tenpai(state.closedHand)
        .filter(combination => combination.waitingOn.contains(tile.repr))
        .map(_ => HandValue(1, 1))
        .headOption
    }
  }

  def computeTsumoOnTile(tile: String, state: PlayerState): Option[HandValue] = {
    TileSetsHelper
      .tenpai(state.closedHand)
      .filter(combination => combination.waitingOn.contains(tile))
      .map(_ => HandValue(1, 1))
      .headOption

  }

  def computeWin(state: PlayerState): Option[(PlayerState, HandValue)] = {
    state.currentTile match {
      case Some(tile) => computeTsumoOnTile(tile.repr, state).map(v => (state, v))
      case None       => //ron
        None
    }
  }
}
