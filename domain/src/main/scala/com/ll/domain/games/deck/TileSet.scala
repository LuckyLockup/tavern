package com.ll.domain.games.deck

sealed trait TileSet

object TileSet {
  case class TilesPair(x: Tile, y: Tile) extends TileSet
  case class Pung(x: Tile, y: Tile, z: Tile) extends TileSet
  case class Chow(x: Tile, y: Tile, z: Tile) extends TileSet

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
    } else if (isChow(x, y, z)) {
      Some(Chow(x, y, z))
    } else {
      None
    }
  }

  def isPair(x: Tile, y: Tile) = {
    isSameNonNumber(x, y) || (isSameNumber(x, y) && isSameSuit(x, y))
  }

  def isPung(x: Tile, y: Tile, z: Tile) = {
    isSameNonNumber(x, y, z) || (isSameSuit(x, y, z) && isSameNumber(x, y, z))
  }

  def isChow(x: Tile, y: Tile, z: Tile) = {
    val sorted = List(x, y, z).sortBy(_.order)
    isSameSuit(x, y, z) && isInRow(sorted(0), sorted(1), sorted(2))
  }

  def isSameNonNumber(x: Tile, y: Tile) = (x, y) match {
    case (_ : Tile.White, _: Tile.White) => true
    case (_ : Tile.Green, _: Tile.Green) => true
    case (_ : Tile.Red, _: Tile.Red) => true
    case (_ : Tile.East, _: Tile.East) => true
    case (_ : Tile.South, _: Tile.South) => true
    case (_ : Tile.West, _: Tile.West) => true
    case (_ : Tile.North, _: Tile.North) => true
    case _ => false
  }

  def isSameNonNumber(x: Tile, y: Tile, z: Tile) = (x, y, z) match {
    case (_ : Tile.White, _: Tile.White, _: Tile.White) => true
    case (_ : Tile.Green, _: Tile.Green, _: Tile.Green) => true
    case (_ : Tile.Red, _: Tile.Red, _: Tile.Red) => true
    case (_ : Tile.East, _: Tile.East, _: Tile.East) => true
    case (_ : Tile.South, _: Tile.South, _: Tile.South) => true
    case (_ : Tile.West, _: Tile.West, _: Tile.West) => true
    case (_ : Tile.North, _: Tile.North, _: Tile.North) => true
    case _ => false
  }

  def isSameSuit(x: Tile, y: Tile): Boolean = (x, y) match {
    case (_: Tile.Pin, y: Tile.Pin) => true
    case (_: Tile.Sou, y: Tile.Sou) => true
    case (_: Tile.Wan, y: Tile.Wan) => true
    case _ => false
  }

  def isSameSuit(x: Tile, y: Tile, z: Tile): Boolean = (x, y, z) match {
    case (_: Tile.Pin, y: Tile.Pin, z: Tile.Pin) => true
    case (_: Tile.Sou, y: Tile.Sou, z: Tile.Sou) => true
    case (_: Tile.Wan, y: Tile.Wan, z: Tile.Wan) => true
    case _ => false
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
    case _ => false
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
    case _ => false
  }

  def isInRow(x: Tile, y: Tile, z: Tile) = (x, y, z) match {
    case (_: Tile.`1`, y: Tile.`2`, z: Tile.`3`) => true
    case (_: Tile.`2`, y: Tile.`3`, z: Tile.`4`) => true
    case (_: Tile.`3`, y: Tile.`4`, z: Tile.`5`) => true
    case (_: Tile.`4`, y: Tile.`5`, z: Tile.`6`) => true
    case (_: Tile.`5`, y: Tile.`6`, z: Tile.`7`) => true
    case (_: Tile.`6`, y: Tile.`7`, z: Tile.`8`) => true
    case (_: Tile.`7`, y: Tile.`8`, z: Tile.`9`) => true
    case _ => false
  }
}
