package com.ll.domain.json

import com.ll.domain.messages.WsMsg
import com.ll.domain.messages.WsMsg.In
import io.circe._
import io.circe.generic.auto._
import io.circe.generic.extras.Configuration
import io.circe.parser._
import io.circe.syntax._


object Codec {
  import JsonConfig._

  def decodeWsMsg(json: String): Either[Error, WsMsg.In] = {

   ???
  }

  def encodeWsMsg(msg: WsMsg.Out): String = {
//    msg.asJson.noSpaces
    ???
  }


  object Test {
    def decodeWsMsg(json: String): Either[Error, WsMsg.Out] = {

      ???
    }

    def encodeWsMsg(msg: WsMsg.In): String = {
      //    msg.asJson.noSpaces
      ???
    }

  }
}
