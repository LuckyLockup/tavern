package com.ll.domain.json

import com.ll.domain.messages.WsMsg
import io.circe.generic.semiauto._
import io.circe.generic.extras.Configuration
//import io.circe.syntax._


object Codec {
  implicit val configuration: Configuration = Configuration.default
    .withDiscriminator("type")
  implicit val pong = deriveEncoder[WsMsg.Out.Pong]

  def decodeWsMsg(json: String): Either[Error, WsMsg.In] = {

   ???
  }

  def encodeWsMsg(msg: WsMsg.Out): String = {
    val json = msg match {
      //    case x: WsMsg.Out.Pong => Json.obj(
      //      "type" -> Json.fromString("Pong"),
      //      "payload" -> x.asJson).noSpaces
      //    case x: WsMsg.Out.Pong => Json.obj(
      //      "type" -> Json.fromString("Pong"),
      //      "payload" -> x.asJson).noSpaces
      case x: WsMsg.Out.Pong => pong.apply(x)
    }

    json.noSpaces
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
