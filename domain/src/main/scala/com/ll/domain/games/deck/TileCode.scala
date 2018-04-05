package com.ll.domain.games.deck

sealed trait TileCode {
  def code: String
}

object TileCode {
  case object Pin1 extends TileCode { def code = "1_pin"}
  case object Pin2 extends TileCode { def code = "2_pin"}
  case object Pin3 extends TileCode { def code = "3_pin"}
  case object Pin4 extends TileCode { def code = "4_pin"}
  case object Pin5 extends TileCode { def code = "5_pin"}
  case object Pin6 extends TileCode { def code = "6_pin"}
  case object Pin7 extends TileCode { def code = "7_pin"}
  case object Pin8 extends TileCode { def code = "8_pin"}
  case object Pin9 extends TileCode { def code = "9_pin"}

  case object Sou1 extends TileCode { def code = "1_sou"}
  case object Sou2 extends TileCode { def code = "2_sou"}
  case object Sou3 extends TileCode { def code = "3_sou"}
  case object Sou4 extends TileCode { def code = "4_sou"}
  case object Sou5 extends TileCode { def code = "5_sou"}
  case object Sou6 extends TileCode { def code = "6_sou"}
  case object Sou7 extends TileCode { def code = "7_sou"}
  case object Sou8 extends TileCode { def code = "8_sou"}
  case object Sou9 extends TileCode { def code = "9_sou"}

  case object Wan1 extends TileCode { def code = "1_wan"}
  case object Wan2 extends TileCode { def code = "2_wan"}
  case object Wan3 extends TileCode { def code = "3_wan"}
  case object Wan4 extends TileCode { def code = "4_wan"}
  case object Wan5 extends TileCode { def code = "5_wan"}
  case object Wan6 extends TileCode { def code = "6_wan"}
  case object Wan7 extends TileCode { def code = "7_wan"}
  case object Wan8 extends TileCode { def code = "8_wan"}
  case object Wan9 extends TileCode { def code = "9_wan"}

  case object East extends TileCode { def code = "east"}
  case object South extends TileCode { def code = "south"}
  case object North extends TileCode { def code = "north"}
  case object West extends TileCode { def code = "west"}

  case object White extends TileCode { def code = "white"}
  case object Green extends TileCode { def code = "green"}
  case object Red extends TileCode { def code = "red"}
}
