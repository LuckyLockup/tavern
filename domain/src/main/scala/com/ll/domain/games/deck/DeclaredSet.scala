package com.ll.domain.games.deck

import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.json.CaseClassCodec
import io.circe.{Decoder, Encoder}

case class DeclaredSet[GT<: GameType](
  set: TileSet,
  from: PlayerPosition[GT],
  turn: Int
)

object DeclaredSet extends CaseClassCodec {
  implicit lazy val TileClaimedEncoder: Encoder[DeclaredSet[Riichi]] = encoder[DeclaredSet[Riichi]]("DeclaredSet")
  implicit lazy val TileClaimedDecoder: Decoder[DeclaredSet[Riichi]] = decoder[DeclaredSet[Riichi]]("DeclaredSet")
}