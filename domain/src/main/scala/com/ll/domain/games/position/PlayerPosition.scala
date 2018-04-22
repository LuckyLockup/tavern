package com.ll.domain.games.position

import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi
import io.circe._
import io.circe.syntax._

sealed trait PlayerPosition[GT <: GameType] {
  def nextPosition: PlayerPosition[GT]
}

object PlayerPosition {
  object RiichiPosition {
    case object EastPosition extends PlayerPosition[GameType.Riichi] {def nextPosition = SouthPosition}
    case object SouthPosition extends PlayerPosition[GameType.Riichi] {def nextPosition = WestPosition}
    case object WestPosition extends PlayerPosition[GameType.Riichi] {def nextPosition = NorthPosition}
    case object NorthPosition extends PlayerPosition[GameType.Riichi] {def nextPosition = EastPosition}
  }

  implicit lazy val positionEncoder: Encoder[PlayerPosition[Riichi]] = {
    case PlayerPosition.RiichiPosition.EastPosition  => "EastPosition".asJson
    case PlayerPosition.RiichiPosition.SouthPosition => "SouthPosition".asJson
    case PlayerPosition.RiichiPosition.WestPosition  => "WestPosition".asJson
    case PlayerPosition.RiichiPosition.NorthPosition => "NorthPosition".asJson
  }

  implicit lazy val positionDecoder: Decoder[PlayerPosition[Riichi]] = (c: HCursor) => {
    def decode(str: String): Decoder.Result[PlayerPosition[Riichi]] = str match {
      case "EastPosition"  => Right(PlayerPosition.RiichiPosition.EastPosition)
      case "SouthPosition" => Right(PlayerPosition.RiichiPosition.SouthPosition)
      case "WestPosition"  => Right(PlayerPosition.RiichiPosition.WestPosition)
      case "NorthPosition" => Right(PlayerPosition.RiichiPosition.NorthPosition)
      case _               => Left(DecodingFailure(s"$str is not known position", Nil))
    }

    c.focus.flatMap(_.asString).map(s => decode(s))
      .getOrElse(Left(DecodingFailure("Can't decode position from", Nil)))
  }
}
