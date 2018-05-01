package com.ll.domain.games.riichi.result

import com.ll.domain.games.GameType
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.json.CaseClassCodec
import io.circe.{Decoder, Encoder}

case class TablePoints(
  points: Map[PlayerPosition[GameType.Riichi], Points]
) {
  def addGameScore(gameScore: GameScore): TablePoints = {
    val updatedPoints = for {
      (position, tablePoints) <- this.points
      gamePoints <- gameScore.points.get(position)
    } yield position -> Points(tablePoints.points + gamePoints.points)
    this.copy(points = updatedPoints)
  }
}

object TablePoints extends CaseClassCodec {
  implicit lazy val TablePointsEncoder: Encoder[TablePoints] = encoder[TablePoints]("TablePoints")
  implicit lazy val TablePointsDecoder: Decoder[TablePoints] = decoder[TablePoints]("TablePoints")

  def initialPoints: TablePoints = TablePoints(
    points = Map(
      PlayerPosition.RiichiPosition.EastPosition -> Points(25000),
      PlayerPosition.RiichiPosition.SouthPosition -> Points(25000),
      PlayerPosition.RiichiPosition.WestPosition -> Points(25000),
      PlayerPosition.RiichiPosition.NorthPosition -> Points(25000)
    )
  )
}
