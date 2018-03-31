package com.ll.domain.games.riichi

sealed trait Tile {
  def repr: String
}

object Tile {
  sealed trait Pin extends Tile
  sealed trait Sou extends Tile
  sealed trait Wan extends Tile

  sealed trait Number
  sealed trait `1` extends Number
  sealed trait `2` extends Number
  sealed trait `3` extends Number
  sealed trait `4` extends Number
  sealed trait `5` extends Number
  sealed trait `6` extends Number
  sealed trait `7` extends Number
  sealed trait `8` extends Number
  sealed trait `9` extends Number

  sealed trait Wind extends Tile
  sealed trait East extends Wind
  sealed trait South extends Wind
  sealed trait West extends Wind
  sealed trait North extends Wind

  sealed trait Dragon extends Tile
  sealed trait White extends Dragon
  sealed trait Green extends Dragon
  sealed trait Red extends Dragon

  case object Wind {
    case object East_1 extends East {def repr = TileCode.East.code}
    case object East_2 extends East {def repr = TileCode.East.code}
    case object East_3 extends East {def repr = TileCode.East.code}
    case object East_4 extends East {def repr = TileCode.East.code}

    case object South_1 extends South {def repr = TileCode.South.code}
    case object South_2 extends South {def repr = TileCode.South.code}
    case object South_3 extends South {def repr = TileCode.South.code}
    case object South_4 extends South {def repr = TileCode.South.code}

    case object West_1 extends West {def repr = TileCode.West.code}
    case object West_2 extends West {def repr = TileCode.West.code}
    case object West_3 extends West {def repr = TileCode.West.code}
    case object West_4 extends West {def repr = TileCode.West.code}

    case object North_1 extends North {def repr = TileCode.North.code}
    case object North_2 extends North {def repr = TileCode.North.code}
    case object North_3 extends North {def repr = TileCode.North.code}
    case object North_4 extends North {def repr = TileCode.North.code}
  }

  case object Dragon {
    case object White_1 extends White {def repr = TileCode.White.code}
    case object White_2 extends White {def repr = TileCode.White.code}
    case object White_3 extends White {def repr = TileCode.White.code}
    case object White_4 extends White {def repr = TileCode.White.code}

    case object Green_1 extends Green {def repr = TileCode.Green.code}
    case object Green_2 extends Green {def repr = TileCode.Green.code}
    case object Green_3 extends Green {def repr = TileCode.Green.code}
    case object Green_4 extends Green {def repr = TileCode.Green.code}

    case object Red_1 extends Red {def repr = TileCode.Red.code}
    case object Red_2 extends Red {def repr = TileCode.Red.code}
    case object Red_3 extends Red {def repr = TileCode.Red.code}
    case object Red_4 extends Red {def repr = TileCode.Red.code}
  }

  case object Pin {
    case object Pin1_1 extends Pin with `1` {def repr = TileCode.Pin1.code}
    case object Pin1_2 extends Pin with `1` {def repr = TileCode.Pin1.code}
    case object Pin1_3 extends Pin with `1` {def repr = TileCode.Pin1.code}
    case object Pin1_4 extends Pin with `1` {def repr = TileCode.Pin1.code}

    case object Pin2_1 extends Pin with `2` {def repr = TileCode.Pin2.code}
    case object Pin2_2 extends Pin with `2` {def repr = TileCode.Pin2.code}
    case object Pin2_3 extends Pin with `2` {def repr = TileCode.Pin2.code}
    case object Pin2_4 extends Pin with `2` {def repr = TileCode.Pin2.code}

    case object Pin3_1 extends Pin with `3` {def repr = TileCode.Pin3.code}
    case object Pin3_2 extends Pin with `3` {def repr = TileCode.Pin3.code}
    case object Pin3_3 extends Pin with `3` {def repr = TileCode.Pin3.code}
    case object Pin3_4 extends Pin with `3` {def repr = TileCode.Pin3.code}

    case object Pin4_1 extends Pin with `4` {def repr = TileCode.Pin4.code}
    case object Pin4_2 extends Pin with `4` {def repr = TileCode.Pin4.code}
    case object Pin4_3 extends Pin with `4` {def repr = TileCode.Pin4.code}
    case object Pin4_4 extends Pin with `4` {def repr = TileCode.Pin4.code}

    case object Pin5_1 extends Pin with `5` {def repr = TileCode.Pin5.code}
    case object Pin5_2 extends Pin with `5` {def repr = TileCode.Pin5.code}
    case object Pin5_3 extends Pin with `5` {def repr = TileCode.Pin5.code}
    case object Pin5_4 extends Pin with `5` {def repr = TileCode.Pin5.code}

    case object Pin6_1 extends Pin with `6` {def repr = TileCode.Pin6.code}
    case object Pin6_2 extends Pin with `6` {def repr = TileCode.Pin6.code}
    case object Pin6_3 extends Pin with `6` {def repr = TileCode.Pin6.code}
    case object Pin6_4 extends Pin with `6` {def repr = TileCode.Pin6.code}

    case object Pin7_1 extends Pin with `7` {def repr = TileCode.Pin7.code}
    case object Pin7_2 extends Pin with `7` {def repr = TileCode.Pin7.code}
    case object Pin7_3 extends Pin with `7` {def repr = TileCode.Pin7.code}
    case object Pin7_4 extends Pin with `7` {def repr = TileCode.Pin7.code}

    case object Pin8_1 extends Pin with `8` {def repr = TileCode.Pin8.code}
    case object Pin8_2 extends Pin with `8` {def repr = TileCode.Pin8.code}
    case object Pin8_3 extends Pin with `8` {def repr = TileCode.Pin8.code}
    case object Pin8_4 extends Pin with `8` {def repr = TileCode.Pin8.code}

    case object Pin9_1 extends Pin with `9` {def repr = TileCode.Pin9.code}
    case object Pin9_2 extends Pin with `9` {def repr = TileCode.Pin9.code}
    case object Pin9_3 extends Pin with `9` {def repr = TileCode.Pin9.code}
    case object Pin9_4 extends Pin with `9` {def repr = TileCode.Pin9.code}
  }

  case object Sou {
    case object Sou1_1 extends Sou with `1` {def repr = TileCode.Sou1.code}
    case object Sou1_2 extends Sou with `1` {def repr = TileCode.Sou1.code}
    case object Sou1_3 extends Sou with `1` {def repr = TileCode.Sou1.code}
    case object Sou1_4 extends Sou with `1` {def repr = TileCode.Sou1.code}

    case object Sou2_1 extends Sou with `2` {def repr = TileCode.Sou2.code}
    case object Sou2_2 extends Sou with `2` {def repr = TileCode.Sou2.code}
    case object Sou2_3 extends Sou with `2` {def repr = TileCode.Sou2.code}
    case object Sou2_4 extends Sou with `2` {def repr = TileCode.Sou2.code}

    case object Sou3_1 extends Sou with `3` {def repr = TileCode.Sou3.code}
    case object Sou3_2 extends Sou with `3` {def repr = TileCode.Sou3.code}
    case object Sou3_3 extends Sou with `3` {def repr = TileCode.Sou3.code}
    case object Sou3_4 extends Sou with `3` {def repr = TileCode.Sou3.code}

    case object Sou4_1 extends Sou with `4` {def repr = TileCode.Sou4.code}
    case object Sou4_2 extends Sou with `4` {def repr = TileCode.Sou4.code}
    case object Sou4_3 extends Sou with `4` {def repr = TileCode.Sou4.code}
    case object Sou4_4 extends Sou with `4` {def repr = TileCode.Sou4.code}

    case object Sou5_1 extends Sou with `5` {def repr = TileCode.Sou5.code}
    case object Sou5_2 extends Sou with `5` {def repr = TileCode.Sou5.code}
    case object Sou5_3 extends Sou with `5` {def repr = TileCode.Sou5.code}
    case object Sou5_4 extends Sou with `5` {def repr = TileCode.Sou5.code}

    case object Sou6_1 extends Sou with `6` {def repr = TileCode.Sou6.code}
    case object Sou6_2 extends Sou with `6` {def repr = TileCode.Sou6.code}
    case object Sou6_3 extends Sou with `6` {def repr = TileCode.Sou6.code}
    case object Sou6_4 extends Sou with `6` {def repr = TileCode.Sou6.code}

    case object Sou7_1 extends Sou with `7` {def repr = TileCode.Sou7.code}
    case object Sou7_2 extends Sou with `7` {def repr = TileCode.Sou7.code}
    case object Sou7_3 extends Sou with `7` {def repr = TileCode.Sou7.code}
    case object Sou7_4 extends Sou with `7` {def repr = TileCode.Sou7.code}

    case object Sou8_1 extends Sou with `8` {def repr = TileCode.Sou8.code}
    case object Sou8_2 extends Sou with `8` {def repr = TileCode.Sou8.code}
    case object Sou8_3 extends Sou with `8` {def repr = TileCode.Sou8.code}
    case object Sou8_4 extends Sou with `8` {def repr = TileCode.Sou8.code}

    case object Sou9_1 extends Sou with `9` {def repr = TileCode.Sou9.code}
    case object Sou9_2 extends Sou with `9` {def repr = TileCode.Sou9.code}
    case object Sou9_3 extends Sou with `9` {def repr = TileCode.Sou9.code}
    case object Sou9_4 extends Sou with `9` {def repr = TileCode.Sou9.code}
  }

  case object Wan {
    case object Wan1_1 extends Wan with `1` {def repr = TileCode.Wan1.code}
    case object Wan1_2 extends Wan with `1` {def repr = TileCode.Wan1.code}
    case object Wan1_3 extends Wan with `1` {def repr = TileCode.Wan1.code}
    case object Wan1_4 extends Wan with `1` {def repr = TileCode.Wan1.code}

    case object Wan2_1 extends Wan with `2` {def repr = TileCode.Wan2.code}
    case object Wan2_2 extends Wan with `2` {def repr = TileCode.Wan2.code}
    case object Wan2_3 extends Wan with `2` {def repr = TileCode.Wan2.code}
    case object Wan2_4 extends Wan with `2` {def repr = TileCode.Wan2.code}

    case object Wan3_1 extends Wan with `3` {def repr = TileCode.Wan3.code}
    case object Wan3_2 extends Wan with `3` {def repr = TileCode.Wan3.code}
    case object Wan3_3 extends Wan with `3` {def repr = TileCode.Wan3.code}
    case object Wan3_4 extends Wan with `3` {def repr = TileCode.Wan3.code}

    case object Wan4_1 extends Wan with `4` {def repr = TileCode.Wan4.code}
    case object Wan4_2 extends Wan with `4` {def repr = TileCode.Wan4.code}
    case object Wan4_3 extends Wan with `4` {def repr = TileCode.Wan4.code}
    case object Wan4_4 extends Wan with `4` {def repr = TileCode.Wan4.code}

    case object Wan5_1 extends Wan with `5` {def repr = TileCode.Wan5.code}
    case object Wan5_2 extends Wan with `5` {def repr = TileCode.Wan5.code}
    case object Wan5_3 extends Wan with `5` {def repr = TileCode.Wan5.code}
    case object Wan5_4 extends Wan with `5` {def repr = TileCode.Wan5.code}

    case object Wan6_1 extends Wan with `6` {def repr = TileCode.Wan6.code}
    case object Wan6_2 extends Wan with `6` {def repr = TileCode.Wan6.code}
    case object Wan6_3 extends Wan with `6` {def repr = TileCode.Wan6.code}
    case object Wan6_4 extends Wan with `6` {def repr = TileCode.Wan6.code}

    case object Wan7_1 extends Wan with `7` {def repr = TileCode.Wan7.code}
    case object Wan7_2 extends Wan with `7` {def repr = TileCode.Wan7.code}
    case object Wan7_3 extends Wan with `7` {def repr = TileCode.Wan7.code}
    case object Wan7_4 extends Wan with `7` {def repr = TileCode.Wan7.code}

    case object Wan8_1 extends Wan with `8` {def repr = TileCode.Wan8.code}
    case object Wan8_2 extends Wan with `8` {def repr = TileCode.Wan8.code}
    case object Wan8_3 extends Wan with `8` {def repr = TileCode.Wan8.code}
    case object Wan8_4 extends Wan with `8` {def repr = TileCode.Wan8.code}

    case object Wan9_1 extends Wan with `9` {def repr = TileCode.Wan9.code}
    case object Wan9_2 extends Wan with `9` {def repr = TileCode.Wan9.code}
    case object Wan9_3 extends Wan with `9` {def repr = TileCode.Wan9.code}
    case object Wan9_4 extends Wan with `9` {def repr = TileCode.Wan9.code}
  }
}
