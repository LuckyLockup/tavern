package com.ll.domain.ai

import com.ll.domain.json.CommonTypesCodec
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor}
import io.circe.syntax._

case class ServiceId(id: Long) extends AnyVal

object ServiceId extends CommonTypesCodec {
  implicit lazy val ServiceIdEncoder: Encoder[ServiceId] = (serviceId: ServiceId) => serviceId.id.asJson
  implicit lazy val ServiceIdDecoder: Decoder[ServiceId] = (c: HCursor) => c.focus.flatMap(_.asNumber).flatMap(_.toInt)
    .map(i => Right(ServiceId(i))).getOrElse(Left(DecodingFailure("Can't decode ServiceId from", Nil)))
}