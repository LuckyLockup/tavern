package com.ll.domain.json

import com.ll.domain.auth.User
import com.ll.domain.messages.WsMsg
import io.circe.{Json, ObjectEncoder}
import io.circe.generic.semiauto._
import io.circe.syntax._



object Codec {
  import JsonConfig._
  implicit val user = deriveEncoder[User]

  def decodeWsMsg(json: String): Either[Error, WsMsg.In] = {

   ???
  }

  private def wrap[T](name: String, msg: T)(implicit encoder: ObjectEncoder[T]) = Json.obj(
    "type" -> name.asJson,
    "payload" -> msg.asJson)

  def encodeWsMsg(msg: WsMsg.Out): String = {
    val json = msg match {
      case x: WsMsg.Out.Pong => wrap("Pong", x)(deriveEncoder[WsMsg.Out.Pong])
      case x: WsMsg.Out.Text => wrap("Text", x)(deriveEncoder[WsMsg.Out.Text])
      case x: WsMsg.Out.Table.SpectacularJoinedTable =>
        wrap("SpectacularJoinedTable", x)(deriveEncoder[WsMsg.Out.Table.SpectacularJoinedTable])
      case x: WsMsg.Out.Table.SpectacularLeftTable =>
        wrap("SpectacularLeftTable", x)(deriveEncoder[WsMsg.Out.Table.SpectacularLeftTable])
      case x: WsMsg.Out.Table.TableState =>
        wrap("TableState", x)(deriveEncoder[WsMsg.Out.Table.TableState])
      case x: WsMsg.Out.Table.GameStarted =>
        wrap("GameStarted", x)(deriveEncoder[WsMsg.Out.Table.GameStarted])
      case x: WsMsg.Out.Table.GamePaused =>
        wrap("GamePaused", x)(deriveEncoder[WsMsg.Out.Table.GamePaused])
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
