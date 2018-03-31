package com.ll.domain.ws

import com.ll.domain.ws.WsMsg._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

object Codec {

  def decodeWsMsg(json: String): Either[Error, In] = {
    decode[WsMsg.In](json)
  }

  def encodeWsMsg(msg: WsMsg.Out): String = {
    msg.asJson.noSpaces
  }
}
