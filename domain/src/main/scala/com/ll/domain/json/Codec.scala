package com.ll.domain.json

import com.ll.domain.auth.User
import com.ll.domain.messages.WsMsg
import io.circe.{Decoder, DecodingFailure, HCursor, Json, ObjectEncoder}
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe._, io.circe.parser._

object Codec {
  import JsonConfig._
  implicit val userEncoder = deriveEncoder[User]
  implicit val userDecoder = deriveDecoder[User]

  def decodeWsMsg(json: String): Either[Error, WsMsg.In] = {
    implicit val inDecoder: Decoder[WsMsg.In] = new Decoder[WsMsg.In] {
      final def apply(c: HCursor): Decoder.Result[WsMsg.In] = {
        def decode(messageType: String, payload: Json): Decoder.Result[WsMsg.In] = messageType match {
          case "Ping" => payload.as[WsMsg.In.Ping](deriveDecoder[WsMsg.In.Ping])
        }
        for {
          messageType <- c.downField("type").as[String]
          payload <- c.downField("payload").focus.toRight(DecodingFailure("payload field is not present", Nil))
          in <- decode(messageType, payload)
        } yield in
      }
    }
    parse(json)
      .flatMap(json => json.as[WsMsg.In](inDecoder))
      .fold(
        failure => Left(DecodingFailure(failure.getMessage(), Nil)),
        json => Right(json)
      )
  }

  private def wrap[T](name: String, msg: T)(implicit encoder: ObjectEncoder[T]) = Json.obj(
    "type" -> name.asJson,
    "payload" -> msg.asJson)

  def encodeWsMsg(msg: WsMsg.Out): String = {
    val json = msg match {
      case x: WsMsg.Out.Pong                         => wrap("Pong", x)(deriveEncoder[WsMsg.Out.Pong])
      case x: WsMsg.Out.Text                         => wrap("Text", x)(deriveEncoder[WsMsg.Out.Text])
      case x: WsMsg.Out.Table.SpectacularJoinedTable =>
        wrap("SpectacularJoinedTable", x)(deriveEncoder[WsMsg.Out.Table.SpectacularJoinedTable])
      case x: WsMsg.Out.Table.SpectacularLeftTable   =>
        wrap("SpectacularLeftTable", x)(deriveEncoder[WsMsg.Out.Table.SpectacularLeftTable])
      case x: WsMsg.Out.Table.TableState             =>
        wrap("TableState", x)(deriveEncoder[WsMsg.Out.Table.TableState])
      case x: WsMsg.Out.Table.GameStarted            =>
        wrap("GameStarted", x)(deriveEncoder[WsMsg.Out.Table.GameStarted])
      case x: WsMsg.Out.Table.GamePaused             =>
        wrap("GamePaused", x)(deriveEncoder[WsMsg.Out.Table.GamePaused])
    }
    json.noSpaces
  }

  object Test {
    implicit val outDecoder: Decoder[WsMsg.Out] = new Decoder[WsMsg.Out] {
      final def apply(c: HCursor): Decoder.Result[WsMsg.Out] = {
        def decode(messageType: String, payload: Json): Decoder.Result[WsMsg.Out] = messageType match {
          case "Pong" => payload.as[WsMsg.Out.Pong](deriveDecoder[WsMsg.Out.Pong])
          case "Text" => payload.as[WsMsg.Out.Text](deriveDecoder[WsMsg.Out.Text])
          case "GameStarted" => payload.as[WsMsg.Out.Table.GameStarted](deriveDecoder[WsMsg.Out.Table.GameStarted])
          case "GamePaused" => payload.as[WsMsg.Out.Table.GamePaused](deriveDecoder[WsMsg.Out.Table.GamePaused])
          case "SpectacularJoinedTable" => payload.as[WsMsg.Out.Table.SpectacularJoinedTable](deriveDecoder[WsMsg.Out.Table.SpectacularJoinedTable])
          case "SpectacularLeftTable" => payload.as[WsMsg.Out.Table.SpectacularLeftTable](deriveDecoder[WsMsg.Out.Table.SpectacularLeftTable])
          case "PlayerJoinedTable" => payload.as[WsMsg.Out.Table.PlayerJoinedTable](deriveDecoder[WsMsg.Out.Table.PlayerJoinedTable])
          case "PlayerLeftTable" => payload.as[WsMsg.Out.Table.PlayerLeftTable](deriveDecoder[WsMsg.Out.Table.PlayerLeftTable])
        }
        for {
          messageType <- c.downField("type").as[String]
          payload <- c.downField("payload").focus.toRight(DecodingFailure("payload field is not present", Nil))
          out <- decode(messageType, payload)
        } yield out
      }
    }

    def decodeWsMsg(json: String): Either[DecodingFailure, WsMsg.Out] = {
      parse(json)
        .flatMap(json => json.as[WsMsg.Out](outDecoder))
        .fold(
          failure => Left(DecodingFailure(failure.getMessage(), Nil)),
          json => Right(json)
        )
    }

    def encodeWsMsg(msg: WsMsg.In): String = {
      val json = msg match {
        case x: WsMsg.In.Ping                         => wrap("Ping", x)(deriveEncoder[WsMsg.In.Ping])
      }
      json.noSpaces
    }

  }
}
