package com.ll.domain.games.riichi.result

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.json.CaseClassCodec
import io.circe.{Decoder, Encoder}

/**
  * @param winner either tenpai players or winning position
  */
case class GameScore (
  winner: Either[List[PlayerPosition[Riichi]], PlayerPosition[Riichi]],
//  points: Map[PlayerPosition[Riichi], Int]
)

object GameScore extends CaseClassCodec  {
  implicit lazy val HumanPlayerEncoder: Encoder[GameScore] = encoder[GameScore]("GameScore")
  implicit lazy val HumanPlayerDecoder: Decoder[GameScore] = decoder[GameScore]("GameScore")
}