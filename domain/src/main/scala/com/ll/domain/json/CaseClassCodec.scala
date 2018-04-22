package com.ll.domain.json

import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.encoding.DerivedObjectEncoder
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe._
import io.circe.syntax._

trait CaseClassCodec extends CommonTypesCodec {
  protected def decoder[T: DerivedDecoder](s: String): Decoder[T] = (c: HCursor) => {
    def decode(messageType: String, payload: Json): Decoder.Result[T] = messageType match {
      case _ if messageType == s => payload.as[T](deriveDecoder[T])
      case _                     => Left(DecodingFailure(s"Message is not $messageType", Nil))
    }

    for {
      messageType <- c.downField("type").as[String]
      payload <- c.downField("payload").focus.toRight(DecodingFailure("payload field is not present", Nil))
      in <- decode(messageType, payload)
    } yield in
  }

  protected def encoder[T: DerivedObjectEncoder](s: String): Encoder[T] = (a: T) =>
    wrap(s, a)(deriveEncoder[T])

  protected def wrap[T](name: String, msg: T)(implicit encoder: ObjectEncoder[T]) = Json.obj(
    "type" -> name.asJson,
    "payload" -> msg.asJson
  )
}
