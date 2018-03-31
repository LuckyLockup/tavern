package com.ll.ws

import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import WsMsg._

object Codec {

  def decodeWsMsg(json: String): Either[Error, In] = {
    decode[WsMsg.In](json)
  }

  def encodeWsMsg(msg: WsMsg.Out): String = {
    msg.asJson.noSpaces
  }
}
