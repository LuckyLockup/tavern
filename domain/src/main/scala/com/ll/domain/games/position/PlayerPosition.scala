package com.ll.domain.games.position

import com.ll.domain.games.GameType
import com.ll.domain.games.GameType.Riichi
import io.circe._
import io.circe.syntax._

sealed trait PlayerPosition[GT <: GameType] {
  def repr: String
  def nextPosition: PlayerPosition[GT]
}

object PlayerPosition {
  object RiichiPosition {
    case object EastPosition extends PlayerPosition[GameType.Riichi] {
      def repr = "EastPosition"
      def nextPosition = SouthPosition
    }
    case object SouthPosition extends PlayerPosition[GameType.Riichi] {
      def repr = "SouthPosition"
      def nextPosition = WestPosition
    }
    case object WestPosition extends PlayerPosition[GameType.Riichi] {
      def repr = "WestPosition"
      def nextPosition = NorthPosition
    }
    case object NorthPosition extends PlayerPosition[GameType.Riichi] {
      def repr = "NorthPosition"
      def nextPosition = EastPosition
    }
  }

  implicit lazy val positionEncoder: Encoder[PlayerPosition[Riichi]] = {
    case pos: PlayerPosition[_]  => pos.repr.asJson
  }

  implicit lazy val positionDecoder: Decoder[PlayerPosition[Riichi]] = (c: HCursor) => {
    c.focus.flatMap(_.asString).map(s => decode(s))
      .getOrElse(Left(DecodingFailure("Can't decode position from", Nil)))
  }

  def decode(str: String): Decoder.Result[PlayerPosition[Riichi]] = str match {
    case "EastPosition"  => Right(PlayerPosition.RiichiPosition.EastPosition)
    case "SouthPosition" => Right(PlayerPosition.RiichiPosition.SouthPosition)
    case "WestPosition"  => Right(PlayerPosition.RiichiPosition.WestPosition)
    case "NorthPosition" => Right(PlayerPosition.RiichiPosition.NorthPosition)
    case _               => Left(DecodingFailure(s"$str is not known position", Nil))
  }
}
