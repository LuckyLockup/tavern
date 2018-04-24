package com.ll.domain.games.deck

sealed trait TileSet

object TileSet {
  case class TilesPair(x: Tile, y: Tile) extends TileSet {
    override def toString: String = x match {
      case t: Tile.Number => s"${t.number}${t.number}${t.suit}"
      case t => s"${t}_$t"
    }
  }
  case class Pung(x: Tile, y: Tile, z: Tile) extends TileSet {
    override def toString: String = x match {
      case t: Tile.Number => s"${t.number}${t.number}${t.number}${t.suit}"
      case t => s"${t}_${t}_$t"
    }
  }
  case class Chow(x: Tile.Number, y: Tile.Number, z: Tile.Number) extends TileSet {
    override def toString: String = s"${x.number}${y.number}${z.number}${x.suit}"
  }

  def getSet(x: Tile, y: Tile): Option[TileSet] = {
    if (isPair(x, y)) {
      Some(TilesPair(x, y))
    } else {
      None
    }
  }

  def getSet(x: Tile, y: Tile, z: Tile): Option[TileSet] = {
    if (isPung(x, y, z)) {
      Some(Pung(x, y, z))
    } else {
      (x,y,z) match {
        case (t1 : Tile.Sou, t2: Tile.Sou, t3: Tile.Sou) if isChow(t1, t2, t3) => Some(Chow(t1, t2, t3))
        case (t1 : Tile.Wan, t2: Tile.Wan, t3: Tile.Wan) if isChow(t1, t2, t3) => Some(Chow(t1, t2, t3))
        case (t1 : Tile.Pin, t2: Tile.Pin, t3: Tile.Pin) if isChow(t1, t2, t3) => Some(Chow(t1, t2, t3))
        case _ => None
      }
    }
  }

  def isPair(x: Tile, y: Tile) = {
    x.repr == y.repr
  }

  def isPung(x: Tile, y: Tile, z: Tile) = {
    x.repr == y.repr && y.repr == z.repr
  }

  def isChow(x: Tile.Number, y:  Tile.Number, z: Tile.Number) = {
    val sorted = List(x, y, z).sortBy(_.order)
    (x.suit == y.suit && y.suit == z.suit) && isInRow(sorted(0), sorted(1), sorted(2))
  }

  def isSameNonNumber(x: Tile, y: Tile) = (x, y) match {
    case (_: Tile.White, _: Tile.White) => true
    case (_: Tile.Green, _: Tile.Green) => true
    case (_: Tile.Red, _: Tile.Red)     => true
    case (_: Tile.East, _: Tile.East)   => true
    case (_: Tile.South, _: Tile.South) => true
    case (_: Tile.West, _: Tile.West)   => true
    case (_: Tile.North, _: Tile.North) => true
    case _                              => false
  }

  def isSameNonNumber(x: Tile, y: Tile, z: Tile) = (x, y, z) match {
    case (_: Tile.White, _: Tile.White, _: Tile.White) => true
    case (_: Tile.Green, _: Tile.Green, _: Tile.Green) => true
    case (_: Tile.Red, _: Tile.Red, _: Tile.Red)       => true
    case (_: Tile.East, _: Tile.East, _: Tile.East)    => true
    case (_: Tile.South, _: Tile.South, _: Tile.South) => true
    case (_: Tile.West, _: Tile.West, _: Tile.West)    => true
    case (_: Tile.North, _: Tile.North, _: Tile.North) => true
    case _                                             => false
  }

  def isSameSuit(x: Tile, y: Tile): Boolean = (x, y) match {
    case (_: Tile.Pin, y: Tile.Pin) => true
    case (_: Tile.Sou, y: Tile.Sou) => true
    case (_: Tile.Wan, y: Tile.Wan) => true
    case _                          => false
  }

  def isSameSuit(x: Tile, y: Tile, z: Tile): Boolean = (x, y, z) match {
    case (_: Tile.Pin, y: Tile.Pin, z: Tile.Pin) => true
    case (_: Tile.Sou, y: Tile.Sou, z: Tile.Sou) => true
    case (_: Tile.Wan, y: Tile.Wan, z: Tile.Wan) => true
    case _                                       => false
  }

  def isSameNumber(x: Tile, y: Tile) = (x, y) match {
    case (_: Tile.`1`, y: Tile.`1`) => true
    case (_: Tile.`2`, y: Tile.`2`) => true
    case (_: Tile.`3`, y: Tile.`3`) => true
    case (_: Tile.`4`, y: Tile.`4`) => true
    case (_: Tile.`5`, y: Tile.`5`) => true
    case (_: Tile.`6`, y: Tile.`6`) => true
    case (_: Tile.`7`, y: Tile.`7`) => true
    case (_: Tile.`8`, y: Tile.`8`) => true
    case (_: Tile.`9`, y: Tile.`9`) => true
    case _                          => false
  }

  def isSameNumber(x: Tile, y: Tile, z: Tile) = (x, y, z) match {
    case (_: Tile.`1`, y: Tile.`1`, z: Tile.`1`) => true
    case (_: Tile.`2`, y: Tile.`2`, z: Tile.`2`) => true
    case (_: Tile.`3`, y: Tile.`3`, z: Tile.`3`) => true
    case (_: Tile.`4`, y: Tile.`4`, z: Tile.`4`) => true
    case (_: Tile.`5`, y: Tile.`5`, z: Tile.`5`) => true
    case (_: Tile.`6`, y: Tile.`6`, z: Tile.`6`) => true
    case (_: Tile.`7`, y: Tile.`7`, z: Tile.`7`) => true
    case (_: Tile.`8`, y: Tile.`8`, z: Tile.`8`) => true
    case (_: Tile.`9`, y: Tile.`9`, z: Tile.`9`) => true
    case _                                       => false
  }

  def isInRow(x: Tile, y: Tile, z: Tile) = (x, y, z) match {
    case (_: Tile.`1`, y: Tile.`2`, z: Tile.`3`) => true
    case (_: Tile.`2`, y: Tile.`3`, z: Tile.`4`) => true
    case (_: Tile.`3`, y: Tile.`4`, z: Tile.`5`) => true
    case (_: Tile.`4`, y: Tile.`5`, z: Tile.`6`) => true
    case (_: Tile.`5`, y: Tile.`6`, z: Tile.`7`) => true
    case (_: Tile.`6`, y: Tile.`7`, z: Tile.`8`) => true
    case (_: Tile.`7`, y: Tile.`8`, z: Tile.`9`) => true
    case _                                       => false
  }
}
