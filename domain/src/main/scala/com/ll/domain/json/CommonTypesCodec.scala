package com.ll.domain.json

import io.circe._
import io.circe.syntax._
import shapeless.{::, Generic, HNil, Lazy}

import scala.concurrent.duration.{Duration, FiniteDuration}


trait CommonTypesCodec {
  implicit protected def encodeCaseObject[A <: Product](implicit
    gen: Generic.Aux[A, HNil]
  ): Encoder[A] = Encoder[String].contramap[A](_.productPrefix)

  implicit protected def encoderValueClass[T <: AnyVal, V](implicit
    g: Lazy[Generic.Aux[T, V :: HNil]],
    e: Encoder[V]
  ): Encoder[T] = Encoder.instance { value ⇒
    e(g.value.to(value).head)
  }

  implicit protected def decoderValueClass[T <: AnyVal, V](implicit
    g: Lazy[Generic.Aux[T, V :: HNil]],
    d: Decoder[V]
  ): Decoder[T] = Decoder.instance { cursor ⇒
    d(cursor).map { value ⇒
      g.value.from(value :: HNil)
    }
  }

  implicit protected def encodeEither[A, B](implicit
    encoderA: Encoder[A],
    encoderB: Encoder[B]
  ): Encoder[Either[A, B]] = {
    o: Either[A, B] => o.fold(_.asJson, _.asJson)
  }

  implicit protected def decodeEither[A, B](implicit
    decoderA: Decoder[A],
    decoderB: Decoder[B]
  ): Decoder[Either[A, B]] = {
    c: HCursor =>
      c.as[A] match {
        case Right(a) => Right(Left(a))
        case _        => c.as[B].map(Right(_))
      }
  }

  //Basic types from other libraries
  implicit lazy val FiniteDurationEncoder: Encoder[FiniteDuration] = (d: FiniteDuration) => Json.fromString(d.toString())
  implicit lazy val FiniteDurationDecoder: Decoder[FiniteDuration] = (c: HCursor) => {
    c.focus.flatMap(_.asString) match {
      case None           => Left(DecodingFailure("not a duration", Nil))
      case Some(duration) => Right(Duration(duration).asInstanceOf[FiniteDuration])
    }
  }
}
