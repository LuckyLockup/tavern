package com.ll.domain.auth

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class User(id: UserId, nickname: String)

object User {
  implicit lazy val UserEncoder: Encoder[User] = deriveEncoder[User]
  implicit lazy val UserDecoder: Decoder[User] = deriveDecoder[User]
}
