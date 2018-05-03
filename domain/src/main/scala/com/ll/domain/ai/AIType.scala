package com.ll.domain.ai

import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi
import io.circe._
import io.circe.syntax._

sealed trait AIType[GT <: GameType] {
  def serviceId: ServiceId
}

object AIType {
  /**
    * This is basic AI variant which can only discard whatever title it gets from the wall.
    */
  object Riichi {
    case object Duck extends AIType[Riichi] {def serviceId = ServiceId(1)}
  }

  implicit lazy val AiTypeEncoder: Encoder[AIType[Riichi]] = {
    case AIType.Riichi.Duck => "Duck".asJson
  }

  implicit lazy val AiTypeDecoder: Decoder[AIType[Riichi]] = (c: HCursor) => {
    def decode(str: String): Decoder.Result[AIType[Riichi]] = str match {
      case "Duck" => Right(AIType.Riichi.Duck)
      case _      => Left(DecodingFailure(s"$str is not known AI type", Nil))
    }

    c.focus.flatMap(_.asString).map(s => decode(s))
      .getOrElse(Left(DecodingFailure("Can't decode AI type from", Nil)))
  }
}