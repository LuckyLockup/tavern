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
  nextTileDelay: FiniteDuration = 10.seconds,
  turnDuration: FiniteDuration = 300.seconds,
  testingTiles: Option[TestingState] = None
)

object RiichiConfig extends CaseClassCodec {
  implicit lazy val RiichiConfigEncoder: Encoder[RiichiConfig] = encoder[RiichiConfig]("RiichiConfig")
  implicit lazy val RiichiConfigDecoder: Decoder[RiichiConfig] = decoder[RiichiConfig]("RiichiConfig")
}

case class TestingState(
  eastHand: List[String] = Nil,
  southHand: List[String] = Nil,
  westHand: List[String] = Nil,
  northHand: List[String] = Nil,
  uraDoras: List[String] = Nil,
  deadWall: List[String] = Nil,
  wall: List[String] = Nil
)

object TestingState extends CaseClassCodec {
  implicit lazy val TestingStateEncoder: Encoder[TestingState] = encoder[TestingState]("TestingState")
  implicit lazy val TestingStateDecoder: Decoder[TestingState] = decoder[TestingState]("TestingState")

}