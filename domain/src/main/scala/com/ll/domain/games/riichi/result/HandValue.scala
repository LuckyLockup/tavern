package com.ll.domain.games.riichi.result

import com.ll.domain.json.CaseClassCodec
import io.circe.{Decoder, Encoder}

case class HandValue(
  miniPoints: Int,
  yakus: Int
)

object HandValue extends CaseClassCodec {
  implicit lazy val PauseGameEncoder: Encoder[HandValue] = encoder[HandValue]("HandValue")
  implicit lazy val PauseGameDecoder: Decoder[HandValue] = decoder[HandValue]("HandValue")
}
