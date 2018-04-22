package com.ll.domain.games.position

import com.ll.domain.games.GameType
import com.ll.domain.json.JsonHelper


class PlayerPositionTest extends JsonHelper {
  val examples: List[(PlayerPosition[GameType.Riichi], String)] = List(
    (PlayerPosition.RiichiPosition.EastPosition,
      """
        |"EastPosition"
        |""".stripMargin),
    (PlayerPosition.RiichiPosition.SouthPosition,
      """
        |"SouthPosition"
        |""".stripMargin),
    (PlayerPosition.RiichiPosition.WestPosition,
      """
        |"WestPosition"
        |""".stripMargin),
    (PlayerPosition.RiichiPosition.NorthPosition,
      """
        |"NorthPosition"
        |""".stripMargin)
  )

  "Encode json" in  {
    testEncoding(examples)
  }

  "Decode json" in {
    testDecoding(examples)
  }
}
