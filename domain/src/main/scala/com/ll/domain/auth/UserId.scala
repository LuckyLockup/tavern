package com.ll.domain.auth

import io.circe._

case class UserId(id: Long) extends AnyVal

object UserId {
  implicit val encodeUserId: Encoder[UserId] = (user: UserId) => Json.fromBigInt(user.id)
  implicit val decodeFoo: Decoder[UserId] = (c: HCursor) =>{
    c.focus.flatMap(_.asNumber).flatMap(_.toLong) match {
      case None => Left(DecodingFailure("not a long", Nil))
      case Some(id) => Right(UserId(id))
    }
  }
}