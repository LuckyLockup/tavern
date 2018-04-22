package com.ll.domain.auth

import com.ll.domain.json.CommonTypesCodec
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor}
import io.circe.syntax._

case class UserId(id: Long) extends AnyVal {
  override def toString: String = id.toString
}

object UserId extends CommonTypesCodec {
  implicit lazy val UserIdEncoder: Encoder[UserId] = (userId: UserId) => userId.id.asJson
  implicit lazy val UserIdDecoder: Decoder[UserId] = (c: HCursor) => c.focus.flatMap(_.asNumber).flatMap(_.toInt)
    .map(i => Right(UserId(i))).getOrElse(Left(DecodingFailure("Can't decode position from", Nil)))
}