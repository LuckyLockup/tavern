package com.ll.domain.ws

import io.circe.DecodingFailure
import io.circe.parser._
import io.circe.syntax._

object WsMsgCodec {

  def decodeWsMsg(json: String): Either[DecodingFailure, WsMsgIn] = {
    parse(json)
      .flatMap(json => json.as[WsMsgIn])
      .fold(
        failure => Left(DecodingFailure(failure.getMessage, Nil)),
        json => Right(json)
      )
  }

  def encodeWsMsg(msg: WsMsgOut): String = {
    msg.asJson.noSpaces
  }

  object Test {
    def decodeWsOutMsg(json: String): Either[DecodingFailure, WsMsgOut] = {
      parse(json)
        .flatMap(json => json.as[WsMsgOut])
        .fold(
          failure => Left(DecodingFailure(failure.getMessage(), Nil)),
          json => Right(json)
        )
    }

    def encodeWsInMsg(msg: WsMsgIn): String = {
      msg.asJson.noSpaces
    }
  }
}
