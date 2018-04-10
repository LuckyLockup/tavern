package com.ll.domain.json

import com.ll.domain.auth.User
import com.ll.domain.games.{Player, TableId}
import com.ll.domain.games.Player.{AIPlayer, HumanPlayer}
import com.ll.domain.messages.WsMsg
import com.ll.domain.messages.WsMsg.Out.Table
import com.ll.domain.persistence.{RiichiCmd, TableCmd, UserCmd}
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json, ObjectEncoder}
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.parser._
import shapeless.{::, Generic, HNil, Lazy}

object Codec {
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

  implicit val tableIdEncoder = deriveEncoder[TableId]
  implicit val tableIdDecoder = deriveDecoder[TableId]
  implicit val userEncoder = deriveEncoder[User]
  implicit val userDecoder = deriveDecoder[User]
  implicit val x: Decoder[Table.PlayerJoinedTable] = deriveEncoder[WsMsg.Out.Table.PlayerJoinedTable]
  implicit val PlayerDecoder: Decoder[Player] = new Decoder[Player] {
    final def apply(c: HCursor): Decoder.Result[Player] = {
      def decode(messageType: String, payload: Json): Decoder.Result[Player] = messageType match {
        case "HumanPlayer" => payload.as[HumanPlayer](deriveDecoder[HumanPlayer])
        case "AIPlayer" => payload.as[AIPlayer](deriveDecoder[AIPlayer])
      }

      for {
        messageType <- c.downField("type").as[String]
        payload <- c.downField("payload").focus.toRight(DecodingFailure("payload field is not present", Nil))
        in <- decode(messageType, payload)
      } yield in
    }
  }

  implicit val PlayerEncoder: Encoder[Player] = new Encoder[Player] {
    final def apply(a: Player): Json = a match {
      case x: HumanPlayer => wrap("HumanPlayer", x)(deriveEncoder[HumanPlayer])
      case x: AIPlayer => wrap("AIPlayer", x)(deriveEncoder[AIPlayer])
    }
  }

  def decodeWsMsg(json: String): Either[DecodingFailure, WsMsg.In] = {
    implicit val inDecoder: Decoder[WsMsg.In] = new Decoder[WsMsg.In] {
      final def apply(c: HCursor): Decoder.Result[WsMsg.In] = {
        def decode(messageType: String, payload: Json): Decoder.Result[WsMsg.In] = messageType match {
          case "Ping" => payload.as[WsMsg.In.Ping](deriveDecoder[WsMsg.In.Ping])
          case "TableCmd.StartGame" => payload.as[TableCmd.StartGame](deriveDecoder[TableCmd.StartGame])
          case "TableCmd.PauseGame" => payload.as[TableCmd.PauseGame](deriveDecoder[TableCmd.PauseGame])
          case "UserCmd.GetState" => payload.as[UserCmd.GetState](deriveDecoder[UserCmd.GetState])
          case "UserCmd.JoinAsPlayer" => payload.as[UserCmd.JoinAsPlayer](deriveDecoder[UserCmd.JoinAsPlayer])
          case "UserCmd.LeftAsPlayer" => payload.as[UserCmd.LeftAsPlayer](deriveDecoder[UserCmd.LeftAsPlayer])
          case "UserCmd.JoinAsSpectacular" => payload.as[UserCmd.JoinAsSpectacular](deriveDecoder[UserCmd.JoinAsSpectacular])
          case "UserCmd.LeftAsSpectacular" => payload.as[UserCmd.LeftAsSpectacular](deriveDecoder[UserCmd.LeftAsSpectacular])
          case "RiichiCmd.DiscardTile" => payload.as[RiichiCmd.DiscardTile](deriveDecoder[RiichiCmd.DiscardTile])
          case "RiichiCmd.GetTileFromWall" => payload.as[RiichiCmd.GetTileFromWall](deriveDecoder[RiichiCmd.GetTileFromWall])
          case "RiichiCmd.ClaimTile" => payload.as[RiichiCmd.ClaimTile](deriveDecoder[RiichiCmd.ClaimTile])
          case "RiichiCmd.DeclareWin" => payload.as[RiichiCmd.DeclareWin](deriveDecoder[RiichiCmd.DeclareWin])
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

  def encodeWsMsg(msg: WsMsg.Out): String = {
    val json = msg match {
      case x: WsMsg.Out.Pong                         => wrap("Pong", x)(deriveEncoder[WsMsg.Out.Pong])
      case x: WsMsg.Out.Message                      => wrap("Message", x)(deriveEncoder[WsMsg.Out.Message])
      case x: WsMsg.Out.ValidationError              => wrap("ValidationError", x)(deriveEncoder[WsMsg.Out.ValidationError])
      case x: WsMsg.Out.Table.PlayerJoinedTable =>
        wrap("PlayerJoinedTable", x)
      case x: WsMsg.Out.Table.PlayerLeftTable   =>
        wrap("PlayerLeftTable", x)(deriveEncoder[WsMsg.Out.Table.PlayerLeftTable])
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
          case "Pong"                   => payload.as[WsMsg.Out.Pong](deriveDecoder[WsMsg.Out.Pong])
          case "Message"                => payload.as[WsMsg.Out.Message](deriveDecoder[WsMsg.Out.Message])
          case "ValidationError"        => payload.as[WsMsg.Out.ValidationError](deriveDecoder[WsMsg.Out.ValidationError])
          case "GameStarted"            => payload.as[WsMsg.Out.Table.GameStarted](deriveDecoder[WsMsg.Out.Table.GameStarted])
          case "GamePaused"             => payload.as[WsMsg.Out.Table.GamePaused](deriveDecoder[WsMsg.Out.Table.GamePaused])
          case "SpectacularJoinedTable" => payload.as[WsMsg.Out.Table.SpectacularJoinedTable](deriveDecoder[WsMsg.Out.Table.SpectacularJoinedTable])
          case "SpectacularLeftTable"   => payload.as[WsMsg.Out.Table.SpectacularLeftTable](deriveDecoder[WsMsg.Out.Table.SpectacularLeftTable])
//          case "PlayerJoinedTable"      => payload.as[WsMsg.Out.Table.PlayerJoinedTable](deriveDecoder[WsMsg.Out.Table.PlayerJoinedTable])
//          case "PlayerLeftTable"        => payload.as[WsMsg.Out.Table.PlayerLeftTable](deriveDecoder[WsMsg.Out.Table.PlayerLeftTable])
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
        case x: WsMsg.In.Ping             => wrap("Ping", x)(deriveEncoder[WsMsg.In.Ping])
//        case x: TableCmd.StartGame        => wrap("TableCmd.StartGame", x)(deriveEncoder[TableCmd.StartGame])
//        case x: TableCmd.PauseGame        => wrap("TableCmd.PauseGame", x)(deriveEncoder[TableCmd.PauseGame])
//        case x: UserCmd.GetState          => wrap("UserCmd.GetState", x)(deriveEncoder[UserCmd.GetState])
//        case x: UserCmd.JoinAsPlayer      => wrap("UserCmd.JoinAsPlayer", x)(deriveEncoder[UserCmd.JoinAsPlayer])
//        case x: UserCmd.LeftAsPlayer      => wrap("UserCmd.LeftAsPlayer", x)(deriveEncoder[UserCmd.LeftAsPlayer])
//        case x: UserCmd.JoinAsSpectacular => wrap("UserCmd.JoinAsSpectacular", x)(deriveEncoder[UserCmd.JoinAsSpectacular])
//        case x: UserCmd.LeftAsSpectacular => wrap("UserCmd.LeftAsSpectacular", x)(deriveEncoder[UserCmd.LeftAsSpectacular])
//        case x: RiichiCmd.DiscardTile => wrap("RiichiCmd.DiscardTile", x)(deriveEncoder[RiichiCmd.DiscardTile])
//        case x: RiichiCmd.GetTileFromWall => wrap("RiichiCmd.GetTileFromWall", x)(deriveEncoder[RiichiCmd.GetTileFromWall])
//        case x: RiichiCmd.ClaimTile => wrap("RiichiCmd.ClaimTile", x)(deriveEncoder[RiichiCmd.ClaimTile])
//        case x: RiichiCmd.DeclareWin => wrap("RiichiCmd.DeclareWin", x)(deriveEncoder[RiichiCmd.DeclareWin])
      }
      json.noSpaces
    }
  }

  private def wrap[T](name: String, msg: T)(implicit encoder: ObjectEncoder[T]) = Json.obj(
    "type" -> name.asJson,
    "payload" -> msg.asJson)
}
