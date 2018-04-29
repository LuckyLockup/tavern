package com.ll.domain.games.riichi

import com.ll.domain.ai.AIType
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.json.CaseClassCodec
import io.circe.{Decoder, Encoder}

import scala.concurrent.duration._

case class RiichiConfig(
  defaultEastAi: AIType[Riichi] = AIType.Riichi.Duck,
  defaultSouthAi: AIType[Riichi] = AIType.Riichi.Duck,
  defaultWestAi: AIType[Riichi] = AIType.Riichi.Duck,
  defaultNorthAi: AIType[Riichi] = AIType.Riichi.Duck,
  nextTileDelay: FiniteDuration = 1.seconds,
  testingTiles: List[String] = Nil
)

object RiichiConfig extends CaseClassCodec {
  implicit lazy val RiichiConfigEncoder: Encoder[RiichiConfig] = encoder[RiichiConfig]("RiichiConfig")
  implicit lazy val RiichiConfigDecoder: Decoder[RiichiConfig] = decoder[RiichiConfig]("RiichiConfig")
}
