package com.ll.domain.games.riichi.result

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.json.CaseClassCodec
import io.circe.{Decoder, Encoder, Json}
import io.circe._
import io.circe.syntax._

/**
  * @param winner either tenpai players or winning position
  */
case class GameScore(
  winner: Either[List[PlayerPosition[Riichi]], PlayerPosition[Riichi]],
  points: Map[PlayerPosition[Riichi], Points]
)

object GameScore extends CaseClassCodec {

  implicit lazy val customMapEncoder: Encoder[Map[PlayerPosition[Riichi], Points]] =
    (a: Map[PlayerPosition[Riichi], Points]) => {
      val values = a.map { case (k, v) => k.repr -> v.asJson }.toList
      Json.obj(values: _*)
    }
  implicit lazy val customMapDecoder: Decoder[Map[PlayerPosition[Riichi], Points]] =
    (c: HCursor) => c.focus
      .map(js => js.as[Map[String, Points]])
        .map {
          case Right(obj) => if (obj.keySet.exists(s => PlayerPosition.decode(s).isLeft)) {
            Left(DecodingFailure(s"${obj.keySet} contains unknown position", Nil))
          } else {
            val result: Map[PlayerPosition[Riichi], Points] = obj.map{case (k,v) => PlayerPosition.decode(k).right.get -> v}
            Right(result)
          }
          case Left(failure) => Left(failure)
        }
      .getOrElse(Left(DecodingFailure(s"Can't decode $c", Nil)))

  implicit lazy val HumanPlayerEncoder: Encoder[GameScore] = encoder[GameScore]("GameScore")
  implicit lazy val HumanPlayerDecoder: Decoder[GameScore] = decoder[GameScore]("GameScore")
}