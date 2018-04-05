package com.ll.domain.games.deck

sealed trait Tile {
  def repr: String
  def order: Int
}

object Tile {
  sealed trait Pin extends Tile
  sealed trait Sou extends Tile
  sealed trait Wan extends Tile

  sealed trait Number extends Tile
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
    case object East_1 extends East {def repr = TileCode.East.code; def order = 1}
    case object East_2 extends East {def repr = TileCode.East.code; def order = 2}
    case object East_3 extends East {def repr = TileCode.East.code; def order = 3}
    case object East_4 extends East {def repr = TileCode.East.code; def order = 4}

    case object South_1 extends South {def repr = TileCode.South.code; def order = 5}
    case object South_2 extends South {def repr = TileCode.South.code; def order = 6}
    case object South_3 extends South {def repr = TileCode.South.code; def order = 7}
    case object South_4 extends South {def repr = TileCode.South.code; def order = 8}

    case object West_1 extends West {def repr = TileCode.West.code; def order = 9}
    case object West_2 extends West {def repr = TileCode.West.code; def order = 10}
    case object West_3 extends West {def repr = TileCode.West.code; def order = 11}
    case object West_4 extends West {def repr = TileCode.West.code; def order = 12}

    case object North_1 extends North {def repr = TileCode.North.code; def order = 13}
    case object North_2 extends North {def repr = TileCode.North.code; def order = 14}
    case object North_3 extends North {def repr = TileCode.North.code; def order = 15}
    case object North_4 extends North {def repr = TileCode.North.code; def order = 16}
  }

  case object Dragon {
    case object White_1 extends White {def repr = TileCode.White.code; def order = 17}
    case object White_2 extends White {def repr = TileCode.White.code; def order = 18}
    case object White_3 extends White {def repr = TileCode.White.code; def order = 19}
    case object White_4 extends White {def repr = TileCode.White.code; def order = 20}

    case object Green_1 extends Green {def repr = TileCode.Green.code; def order = 21}
    case object Green_2 extends Green {def repr = TileCode.Green.code; def order = 22}
    case object Green_3 extends Green {def repr = TileCode.Green.code; def order = 23}
    case object Green_4 extends Green {def repr = TileCode.Green.code; def order = 24}

    case object Red_1 extends Red {def repr = TileCode.Red.code; def order = 25}
    case object Red_2 extends Red {def repr = TileCode.Red.code; def order = 26}
    case object Red_3 extends Red {def repr = TileCode.Red.code; def order = 27}
    case object Red_4 extends Red {def repr = TileCode.Red.code; def order = 28}
  }

  case object Pin {
    case object Pin1_1 extends Pin with `1` {def repr = TileCode.Pin1.code; def order = 29}
    case object Pin1_2 extends Pin with `1` {def repr = TileCode.Pin1.code; def order = 30}
    case object Pin1_3 extends Pin with `1` {def repr = TileCode.Pin1.code; def order = 31}
    case object Pin1_4 extends Pin with `1` {def repr = TileCode.Pin1.code; def order = 32}

    case object Pin2_1 extends Pin with `2` {def repr = TileCode.Pin2.code; def order = 33}
    case object Pin2_2 extends Pin with `2` {def repr = TileCode.Pin2.code; def order = 34}
    case object Pin2_3 extends Pin with `2` {def repr = TileCode.Pin2.code; def order = 35}
    case object Pin2_4 extends Pin with `2` {def repr = TileCode.Pin2.code; def order = 36}

    case object Pin3_1 extends Pin with `3` {def repr = TileCode.Pin3.code; def order = 37}
    case object Pin3_2 extends Pin with `3` {def repr = TileCode.Pin3.code; def order = 38}
    case object Pin3_3 extends Pin with `3` {def repr = TileCode.Pin3.code; def order = 39}
    case object Pin3_4 extends Pin with `3` {def repr = TileCode.Pin3.code; def order = 40}

    case object Pin4_1 extends Pin with `4` {def repr = TileCode.Pin4.code; def order = 41}
    case object Pin4_2 extends Pin with `4` {def repr = TileCode.Pin4.code; def order = 42}
    case object Pin4_3 extends Pin with `4` {def repr = TileCode.Pin4.code; def order = 43}
    case object Pin4_4 extends Pin with `4` {def repr = TileCode.Pin4.code; def order = 44}

    case object Pin5_1 extends Pin with `5` {def repr = TileCode.Pin5.code; def order = 45}
    case object Pin5_2 extends Pin with `5` {def repr = TileCode.Pin5.code; def order = 46}
    case object Pin5_3 extends Pin with `5` {def repr = TileCode.Pin5.code; def order = 47}
    case object Pin5_4 extends Pin with `5` {def repr = TileCode.Pin5.code; def order = 48}

    case object Pin6_1 extends Pin with `6` {def repr = TileCode.Pin6.code; def order = 49}
    case object Pin6_2 extends Pin with `6` {def repr = TileCode.Pin6.code; def order = 50}
    case object Pin6_3 extends Pin with `6` {def repr = TileCode.Pin6.code; def order = 51}
    case object Pin6_4 extends Pin with `6` {def repr = TileCode.Pin6.code; def order = 52}

    case object Pin7_1 extends Pin with `7` {def repr = TileCode.Pin7.code; def order = 53}
    case object Pin7_2 extends Pin with `7` {def repr = TileCode.Pin7.code; def order = 54}
    case object Pin7_3 extends Pin with `7` {def repr = TileCode.Pin7.code; def order = 55}
    case object Pin7_4 extends Pin with `7` {def repr = TileCode.Pin7.code; def order = 56}

    case object Pin8_1 extends Pin with `8` {def repr = TileCode.Pin8.code; def order = 57}
    case object Pin8_2 extends Pin with `8` {def repr = TileCode.Pin8.code; def order = 58}
    case object Pin8_3 extends Pin with `8` {def repr = TileCode.Pin8.code; def order = 59}
    case object Pin8_4 extends Pin with `8` {def repr = TileCode.Pin8.code; def order = 60}

    case object Pin9_1 extends Pin with `9` {def repr = TileCode.Pin9.code; def order = 61}
    case object Pin9_2 extends Pin with `9` {def repr = TileCode.Pin9.code; def order = 62}
    case object Pin9_3 extends Pin with `9` {def repr = TileCode.Pin9.code; def order = 63}
    case object Pin9_4 extends Pin with `9` {def repr = TileCode.Pin9.code; def order = 64}
  }

  case object Sou {
    case object Sou1_1 extends Sou with `1` {def repr = TileCode.Sou1.code; def order = 65}
    case object Sou1_2 extends Sou with `1` {def repr = TileCode.Sou1.code; def order = 66}
    case object Sou1_3 extends Sou with `1` {def repr = TileCode.Sou1.code; def order = 67}
    case object Sou1_4 extends Sou with `1` {def repr = TileCode.Sou1.code; def order = 68}

    case object Sou2_1 extends Sou with `2` {def repr = TileCode.Sou2.code; def order = 69}
    case object Sou2_2 extends Sou with `2` {def repr = TileCode.Sou2.code; def order = 70}
    case object Sou2_3 extends Sou with `2` {def repr = TileCode.Sou2.code; def order = 71}
    case object Sou2_4 extends Sou with `2` {def repr = TileCode.Sou2.code; def order = 72}

    case object Sou3_1 extends Sou with `3` {def repr = TileCode.Sou3.code; def order = 73}
    case object Sou3_2 extends Sou with `3` {def repr = TileCode.Sou3.code; def order = 74}
    case object Sou3_3 extends Sou with `3` {def repr = TileCode.Sou3.code; def order = 75}
    case object Sou3_4 extends Sou with `3` {def repr = TileCode.Sou3.code; def order = 76}

    case object Sou4_1 extends Sou with `4` {def repr = TileCode.Sou4.code; def order = 77}
    case object Sou4_2 extends Sou with `4` {def repr = TileCode.Sou4.code; def order = 78}
    case object Sou4_3 extends Sou with `4` {def repr = TileCode.Sou4.code; def order = 79}
    case object Sou4_4 extends Sou with `4` {def repr = TileCode.Sou4.code; def order = 80}

    case object Sou5_1 extends Sou with `5` {def repr = TileCode.Sou5.code; def order = 81}
    case object Sou5_2 extends Sou with `5` {def repr = TileCode.Sou5.code; def order = 82}
    case object Sou5_3 extends Sou with `5` {def repr = TileCode.Sou5.code; def order = 83}
    case object Sou5_4 extends Sou with `5` {def repr = TileCode.Sou5.code; def order = 84}

    case object Sou6_1 extends Sou with `6` {def repr = TileCode.Sou6.code; def order = 85}
    case object Sou6_2 extends Sou with `6` {def repr = TileCode.Sou6.code; def order = 86}
    case object Sou6_3 extends Sou with `6` {def repr = TileCode.Sou6.code; def order = 87}
    case object Sou6_4 extends Sou with `6` {def repr = TileCode.Sou6.code; def order = 88}

    case object Sou7_1 extends Sou with `7` {def repr = TileCode.Sou7.code; def order = 89}
    case object Sou7_2 extends Sou with `7` {def repr = TileCode.Sou7.code; def order = 90}
    case object Sou7_3 extends Sou with `7` {def repr = TileCode.Sou7.code; def order = 91}
    case object Sou7_4 extends Sou with `7` {def repr = TileCode.Sou7.code; def order = 92}

    case object Sou8_1 extends Sou with `8` {def repr = TileCode.Sou8.code; def order = 93}
    case object Sou8_2 extends Sou with `8` {def repr = TileCode.Sou8.code; def order = 94}
    case object Sou8_3 extends Sou with `8` {def repr = TileCode.Sou8.code; def order = 95}
    case object Sou8_4 extends Sou with `8` {def repr = TileCode.Sou8.code; def order = 96}

    case object Sou9_1 extends Sou with `9` {def repr = TileCode.Sou9.code; def order = 97}
    case object Sou9_2 extends Sou with `9` {def repr = TileCode.Sou9.code; def order = 98}
    case object Sou9_3 extends Sou with `9` {def repr = TileCode.Sou9.code; def order = 99}
    case object Sou9_4 extends Sou with `9` {def repr = TileCode.Sou9.code; def order = 100}
  }

  case object Wan {
    case object Wan1_1 extends Wan with `1` {def repr = TileCode.Wan1.code; def order = 101}
    case object Wan1_2 extends Wan with `1` {def repr = TileCode.Wan1.code; def order = 102}
    case object Wan1_3 extends Wan with `1` {def repr = TileCode.Wan1.code; def order = 103}
    case object Wan1_4 extends Wan with `1` {def repr = TileCode.Wan1.code; def order = 104}

    case object Wan2_1 extends Wan with `2` {def repr = TileCode.Wan2.code; def order = 105}
    case object Wan2_2 extends Wan with `2` {def repr = TileCode.Wan2.code; def order = 106}
    case object Wan2_3 extends Wan with `2` {def repr = TileCode.Wan2.code; def order = 107}
    case object Wan2_4 extends Wan with `2` {def repr = TileCode.Wan2.code; def order = 108}

    case object Wan3_1 extends Wan with `3` {def repr = TileCode.Wan3.code; def order = 109}
    case object Wan3_2 extends Wan with `3` {def repr = TileCode.Wan3.code; def order = 110}
    case object Wan3_3 extends Wan with `3` {def repr = TileCode.Wan3.code; def order = 111}
    case object Wan3_4 extends Wan with `3` {def repr = TileCode.Wan3.code; def order = 112}

    case object Wan4_1 extends Wan with `4` {def repr = TileCode.Wan4.code; def order = 113}
    case object Wan4_2 extends Wan with `4` {def repr = TileCode.Wan4.code; def order = 114}
    case object Wan4_3 extends Wan with `4` {def repr = TileCode.Wan4.code; def order = 115}
    case object Wan4_4 extends Wan with `4` {def repr = TileCode.Wan4.code; def order = 116}

    case object Wan5_1 extends Wan with `5` {def repr = TileCode.Wan5.code; def order = 117}
    case object Wan5_2 extends Wan with `5` {def repr = TileCode.Wan5.code; def order = 118}
    case object Wan5_3 extends Wan with `5` {def repr = TileCode.Wan5.code; def order = 119}
    case object Wan5_4 extends Wan with `5` {def repr = TileCode.Wan5.code; def order = 120}

    case object Wan6_1 extends Wan with `6` {def repr = TileCode.Wan6.code; def order = 121}
    case object Wan6_2 extends Wan with `6` {def repr = TileCode.Wan6.code; def order = 122}
    case object Wan6_3 extends Wan with `6` {def repr = TileCode.Wan6.code; def order = 123}
    case object Wan6_4 extends Wan with `6` {def repr = TileCode.Wan6.code; def order = 124}

    case object Wan7_1 extends Wan with `7` {def repr = TileCode.Wan7.code; def order = 125}
    case object Wan7_2 extends Wan with `7` {def repr = TileCode.Wan7.code; def order = 126}
    case object Wan7_3 extends Wan with `7` {def repr = TileCode.Wan7.code; def order = 127}
    case object Wan7_4 extends Wan with `7` {def repr = TileCode.Wan7.code; def order = 128}

    case object Wan8_1 extends Wan with `8` {def repr = TileCode.Wan8.code; def order = 129}
    case object Wan8_2 extends Wan with `8` {def repr = TileCode.Wan8.code; def order = 130}
    case object Wan8_3 extends Wan with `8` {def repr = TileCode.Wan8.code; def order = 131}
    case object Wan8_4 extends Wan with `8` {def repr = TileCode.Wan8.code; def order = 132}

    case object Wan9_1 extends Wan with `9` {def repr = TileCode.Wan9.code; def order = 133}
    case object Wan9_2 extends Wan with `9` {def repr = TileCode.Wan9.code; def order = 134}
    case object Wan9_3 extends Wan with `9` {def repr = TileCode.Wan9.code; def order = 135}
    case object Wan9_4 extends Wan with `9` {def repr = TileCode.Wan9.code; def order = 136}
  }

  val allTiles = List(
    Tile.Pin.Pin1_1, Tile.Pin.Pin1_2, Tile.Pin.Pin1_3, Tile.Pin.Pin1_4,
    Tile.Pin.Pin2_1, Tile.Pin.Pin2_2, Tile.Pin.Pin2_3, Tile.Pin.Pin2_4,
    Tile.Pin.Pin3_1, Tile.Pin.Pin3_2, Tile.Pin.Pin3_3, Tile.Pin.Pin3_4,
    Tile.Pin.Pin4_1, Tile.Pin.Pin4_2, Tile.Pin.Pin4_3, Tile.Pin.Pin4_4,
    Tile.Pin.Pin5_1, Tile.Pin.Pin5_2, Tile.Pin.Pin5_3, Tile.Pin.Pin5_4,
    Tile.Pin.Pin6_1, Tile.Pin.Pin6_2, Tile.Pin.Pin6_3, Tile.Pin.Pin6_4,
    Tile.Pin.Pin7_1, Tile.Pin.Pin7_2, Tile.Pin.Pin7_3, Tile.Pin.Pin7_4,
    Tile.Pin.Pin8_1, Tile.Pin.Pin8_2, Tile.Pin.Pin8_3, Tile.Pin.Pin8_4,
    Tile.Pin.Pin9_1, Tile.Pin.Pin9_2, Tile.Pin.Pin9_3, Tile.Pin.Pin9_4,

    Tile.Sou.Sou1_1, Tile.Sou.Sou1_2, Tile.Sou.Sou1_3, Tile.Sou.Sou1_4,
    Tile.Sou.Sou2_1, Tile.Sou.Sou2_2, Tile.Sou.Sou2_3, Tile.Sou.Sou2_4,
    Tile.Sou.Sou3_1, Tile.Sou.Sou3_2, Tile.Sou.Sou3_3, Tile.Sou.Sou3_4,
    Tile.Sou.Sou4_1, Tile.Sou.Sou4_2, Tile.Sou.Sou4_3, Tile.Sou.Sou4_4,
    Tile.Sou.Sou5_1, Tile.Sou.Sou5_2, Tile.Sou.Sou5_3, Tile.Sou.Sou5_4,
    Tile.Sou.Sou6_1, Tile.Sou.Sou6_2, Tile.Sou.Sou6_3, Tile.Sou.Sou6_4,
    Tile.Sou.Sou7_1, Tile.Sou.Sou7_2, Tile.Sou.Sou7_3, Tile.Sou.Sou7_4,
    Tile.Sou.Sou8_1, Tile.Sou.Sou8_2, Tile.Sou.Sou8_3, Tile.Sou.Sou8_4,
    Tile.Sou.Sou9_1, Tile.Sou.Sou9_2, Tile.Sou.Sou9_3, Tile.Sou.Sou9_4,

    Tile.Wan.Wan1_1, Tile.Wan.Wan1_2, Tile.Wan.Wan1_3, Tile.Wan.Wan1_4,
    Tile.Wan.Wan2_1, Tile.Wan.Wan2_2, Tile.Wan.Wan2_3, Tile.Wan.Wan2_4,
    Tile.Wan.Wan3_1, Tile.Wan.Wan3_2, Tile.Wan.Wan3_3, Tile.Wan.Wan3_4,
    Tile.Wan.Wan4_1, Tile.Wan.Wan4_2, Tile.Wan.Wan4_3, Tile.Wan.Wan4_4,
    Tile.Wan.Wan5_1, Tile.Wan.Wan5_2, Tile.Wan.Wan5_3, Tile.Wan.Wan5_4,
    Tile.Wan.Wan6_1, Tile.Wan.Wan6_2, Tile.Wan.Wan6_3, Tile.Wan.Wan6_4,
    Tile.Wan.Wan7_1, Tile.Wan.Wan7_2, Tile.Wan.Wan7_3, Tile.Wan.Wan7_4,
    Tile.Wan.Wan8_1, Tile.Wan.Wan8_2, Tile.Wan.Wan8_3, Tile.Wan.Wan8_4,
    Tile.Wan.Wan9_1, Tile.Wan.Wan9_2, Tile.Wan.Wan9_3, Tile.Wan.Wan9_4,

    Tile.Wind.East_1, Tile.Wind.East_2, Tile.Wind.East_3, Tile.Wind.East_4,
    Tile.Wind.South_1, Tile.Wind.South_2, Tile.Wind.South_3, Tile.Wind.South_4,
    Tile.Wind.West_1, Tile.Wind.West_2, Tile.Wind.West_3, Tile.Wind.West_4,
    Tile.Wind.North_1, Tile.Wind.North_2, Tile.Wind.North_3, Tile.Wind.North_4,

    Tile.Dragon.White_1, Tile.Dragon.White_2, Tile.Dragon.White_3, Tile.Dragon.White_4,
    Tile.Dragon.Green_1, Tile.Dragon.Green_2, Tile.Dragon.Green_3, Tile.Dragon.Green_4,
    Tile.Dragon.Red_1, Tile.Dragon.Red_2, Tile.Dragon.Red_3, Tile.Dragon.Red_4,
  )
}
