package com.ll.domain.json

import io.circe.generic.extras.Configuration
import io.circe.{Decoder, Encoder}
import shapeless._

object JsonConfig {


  implicit def encoderValueClass[T <: AnyVal, V](implicit
    g: Lazy[Generic.Aux[T, V :: HNil]],
    e: Encoder[V]
  ): Encoder[T] = Encoder.instance { value ⇒
    e(g.value.to(value).head)
  }
  implicit def decoderValueClass[T <: AnyVal, V](implicit
    g: Lazy[Generic.Aux[T, V :: HNil]],
    d: Decoder[V]
  ): Decoder[T] = Decoder.instance { cursor ⇒
    d(cursor).map { value ⇒
      g.value.from(value :: HNil)
    }
  }
}
