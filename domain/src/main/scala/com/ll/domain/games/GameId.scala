package com.ll.domain.games

import io.circe._

case class GameId(id: Long) extends AnyVal

object GameId {
  implicit val encodeUserId: Encoder[GameId] = (game: GameId) => Json.fromBigInt(game.id)
  implicit val decodeFoo: Decoder[GameId] = (c: HCursor) =>{
    c.focus.flatMap(_.asNumber).flatMap(_.toLong) match {
      case None => Left(DecodingFailure("not a long", Nil))
      case Some(id) => Right(GameId(id))
    }
  }
}