package com.ll.domain.json

import io.circe.{Decoder, DecodingFailure, Encoder}
import org.scalatest.{Matchers, WordSpec}
import io.circe.syntax._
import gnieh.diffson.circe._
import io.circe.parser.parse

trait JsonHelper extends WordSpec with Matchers {

  def testEncoding[T](data: List[(T, String)])(implicit encoder: Encoder[T]) = {
    data.foreach{
      case (cls, string) =>
        println(cls.asJson.noSpaces)
        JsonDiff.diff(cls.asJson.noSpaces, string, false).ops shouldBe empty
    }
  }

  def testDecoding[T](data: List[(T, String)])(implicit d: Decoder[T]) = {
    data.foreach{
      case (cls, string) =>
        parse(string)
          .flatMap(json => json.as[T])
          .fold(
            failure => Left(DecodingFailure(failure.getMessage(), Nil)),
            json => Right(json)
          )
    }
  }
}
