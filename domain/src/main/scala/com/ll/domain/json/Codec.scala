package com.ll.domain.json

import com.ll.domain.ai.AIType
import com.ll.domain.auth.User
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.Riichi.{AIPlayer, HumanPlayer}
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.{Player, TableId}
import com.ll.domain.messages.WsMsg
import com.ll.domain.persistence.{RiichiCmd, TableCmd, UserCmd}
import io.circe.generic.decoding.DerivedDecoder
import io.circe.generic.encoding.DerivedObjectEncoder
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json, ObjectEncoder}
import io.circe.generic.semiauto._
import io.circe.syntax._
import io.circe.parser._
import shapeless.{::, Generic, HNil, Lazy}

object Codec {
  implicit def encodeCaseObject[A <: Product](implicit
    gen: Generic.Aux[A, HNil]
  ): Encoder[A] = Encoder[String].contramap[A](_.productPrefix)

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

  implicit def encodeEither[A, B](implicit
    encoderA: Encoder[A],
    encoderB: Encoder[B]
  ): Encoder[Either[A, B]] = {
    o: Either[A, B] =>
      o.fold(_.asJson, _.asJson)
  }

  implicit def decodeEither[A, B](implicit
    decoderA: Decoder[A],
    decoderB: Decoder[B]
  ): Decoder[Either[A, B]] = {
    c: HCursor =>
      c.as[A] match {
        case Right(a) => Right(Left(a))
        case _        => c.as[B].map(Right(_))
      }
  }

  def decoder[T: DerivedDecoder](s: String): Decoder[T] = new Decoder[T] {
    final def apply(c: HCursor): Decoder.Result[T] = {
      def decode(messageType: String, payload: Json): Decoder.Result[T] = messageType match {
        case _ if messageType == s => payload.as[T](deriveDecoder[T])
      }

      for {
        messageType <- c.downField("type").as[String]
        payload <- c.downField("payload").focus.toRight(DecodingFailure("payload field is not present", Nil))
        in <- decode(messageType, payload)
      } yield in
    }
  }

  def encoder[T: DerivedObjectEncoder](s: String): Encoder[T] = new Encoder[T] {
    final def apply(a: T): Json = wrap(s, a)(deriveEncoder[T])
  }

  implicit val encodeTableId: Encoder[TableId] = (table: TableId) => Json.fromString(table.id)
  implicit val decodeTableId: Decoder[TableId] = (c: HCursor) => {
    c.focus.flatMap(_.asString) match {
      case None     => Left(DecodingFailure("not a string", Nil))
      case Some(id) => Right(TableId(id))
    }
  }

  implicit lazy val aiTypeEncoder: Encoder[AIType[Riichi]] = new Encoder[AIType[Riichi]] {
    final def apply(a: AIType[Riichi]): Json = a match {
      case AIType.Riichi.Duck => "Duck".asJson
    }
  }
  implicit lazy val aiTypeDecoder: Decoder[AIType[Riichi]] = new Decoder[AIType[Riichi]] {
    final def apply(c: HCursor): Decoder.Result[AIType[Riichi]] = {
      def decode(str: String): Decoder.Result[AIType[Riichi]] = str match {
        case "Duck" => Right(AIType.Riichi.Duck)
        case _      => Left(DecodingFailure(s"$str is not known AI type", Nil))
      }

      c.focus.flatMap(_.asString).map(s => decode(s))
        .getOrElse(Left(DecodingFailure("Can't decode AI type from", Nil)))
    }
  }

  implicit lazy val userEncoder = deriveEncoder[User]
  implicit lazy val userDecoder = deriveDecoder[User]

  //Player positions
  implicit lazy val positionEncoder: Encoder[PlayerPosition[Riichi]] = new Encoder[PlayerPosition[Riichi]] {
    final def apply(a: PlayerPosition[Riichi]): Json = a match {
      case PlayerPosition.RiichiPosition.EastPosition  => "EastPosition".asJson
      case PlayerPosition.RiichiPosition.SouthPosition => "SouthPosition".asJson
      case PlayerPosition.RiichiPosition.WestPosition  => "WestPosition".asJson
      case PlayerPosition.RiichiPosition.NorthPosition => "NorthPosition".asJson
    }
  }

  implicit lazy val positionDecoder: Decoder[PlayerPosition[Riichi]] = new Decoder[PlayerPosition[Riichi]] {
    final def apply(c: HCursor): Decoder.Result[PlayerPosition[Riichi]] = {
      def decode(str: String): Decoder.Result[PlayerPosition[Riichi]] = str match {
        case "EastPosition"  => Right(PlayerPosition.RiichiPosition.EastPosition)
        case "SouthPosition" => Right(PlayerPosition.RiichiPosition.SouthPosition)
        case "WestPosition"  => Right(PlayerPosition.RiichiPosition.WestPosition)
        case "NorthPosition" => Right(PlayerPosition.RiichiPosition.NorthPosition)
        case _               => Left(DecodingFailure(s"$str is not known position", Nil))
      }

      c.focus.flatMap(_.asString).map(s => decode(s))
        .getOrElse(Left(DecodingFailure("Can't decode position from", Nil)))
    }
  }

  //Players
  implicit lazy val humanPlayerEncoder = encoder[HumanPlayer]("HumanPlayer")
  implicit lazy val humanPlayerDecoder = decoder[HumanPlayer]("AIPlayer")
  implicit lazy val AIPlayerEncoder = encoder[AIPlayer]("AIPlayer")
  implicit lazy val AIPlayerDecoder = decoder[AIPlayer]("AIPlayer")
  implicit lazy val PlayerEncoder = deriveEncoder[Player[Riichi]]
  implicit lazy val PlayerDecoder = deriveDecoder[Player[Riichi]]

  def decodeWsMsg(json: String): Either[DecodingFailure, WsMsg.In] = {
    implicit val inDecoder: Decoder[WsMsg.In] = new Decoder[WsMsg.In] {
      final def apply(c: HCursor): Decoder.Result[WsMsg.In] = {
        def decode(messageType: String, payload: Json): Decoder.Result[WsMsg.In] = messageType match {
          case "Ping"              => payload.as[WsMsg.In.Ping](deriveDecoder[WsMsg.In.Ping])
          case "StartGame"         => payload.as[RiichiCmd.StartGame](deriveDecoder[RiichiCmd.StartGame])
          case "PauseGame"         => payload.as[RiichiCmd.PauseGame](deriveDecoder[RiichiCmd.PauseGame])
          case "GetState"          => payload.as[UserCmd.GetState](deriveDecoder[UserCmd.GetState])
          case "JoinAsPlayer"      => payload.as[UserCmd.JoinAsPlayer](deriveDecoder[UserCmd.JoinAsPlayer])
          case "LeftAsPlayer"      => payload.as[UserCmd.LeftAsPlayer](deriveDecoder[UserCmd.LeftAsPlayer])
          case "JoinAsSpectacular" => payload.as[UserCmd.JoinAsSpectacular](deriveDecoder[UserCmd.JoinAsSpectacular])
          case "LeftAsSpectacular" => payload.as[UserCmd.LeftAsSpectacular](deriveDecoder[UserCmd.LeftAsSpectacular])
          case "DiscardTile"       => payload.as[RiichiCmd.DiscardTile](deriveDecoder[RiichiCmd.DiscardTile])
          case "GetTileFromWall"   => payload.as[RiichiCmd.GetTileFromWall](deriveDecoder[RiichiCmd.GetTileFromWall])
          case "ClaimTile"         => payload.as[RiichiCmd.ClaimTile](deriveDecoder[RiichiCmd.ClaimTile])
          case "DeclareWin"        => payload.as[RiichiCmd.DeclareWin](deriveDecoder[RiichiCmd.DeclareWin])
          case _                   => Left(DecodingFailure(s"$messageType is not known type", Nil))
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
      case x: WsMsg.Out.Pong            => wrap("Pong", x)(deriveEncoder[WsMsg.Out.Pong])
      case x: WsMsg.Out.Message         => wrap("Message", x)(deriveEncoder[WsMsg.Out.Message])
      case x: WsMsg.Out.ValidationError => wrap("ValidationError", x)(deriveEncoder[WsMsg.Out.ValidationError])
      case x: WsMsg.Out.Riichi.PlayerJoinedTable      =>
        wrap("PlayerJoinedTable", x)(deriveEncoder[WsMsg.Out.Riichi.PlayerJoinedTable])
      case x: WsMsg.Out.Riichi.PlayerLeftTable        =>
        wrap("PlayerLeftTable", x)(deriveEncoder[WsMsg.Out.Riichi.PlayerLeftTable])
      case x: WsMsg.Out.Riichi.SpectacularJoinedTable =>
        wrap("SpectacularJoinedTable", x)(deriveEncoder[WsMsg.Out.Riichi.SpectacularJoinedTable])
      case x: WsMsg.Out.Riichi.SpectacularLeftTable   =>
        wrap("SpectacularLeftTable", x)(deriveEncoder[WsMsg.Out.Riichi.SpectacularLeftTable])
      case x: WsMsg.Out.Riichi.RiichiState            =>
        wrap("RiichiState", x)(deriveEncoder[WsMsg.Out.Riichi.RiichiState])
      case x: WsMsg.Out.Riichi.GameStarted            =>
        wrap("GameStarted", x)(deriveEncoder[WsMsg.Out.Riichi.GameStarted])
      case x: WsMsg.Out.Riichi.GamePaused             =>
        wrap("GamePaused", x)(deriveEncoder[WsMsg.Out.Riichi.GamePaused])
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
          case "GameStarted"            => payload.as[WsMsg.Out.Riichi.GameStarted](deriveDecoder[WsMsg.Out.Riichi.GameStarted])
          case "GamePaused"             => payload.as[WsMsg.Out.Riichi.GamePaused](deriveDecoder[WsMsg.Out.Riichi.GamePaused])
          case "SpectacularJoinedTable" => payload.as[WsMsg.Out.Riichi.SpectacularJoinedTable](deriveDecoder[WsMsg.Out.Riichi.SpectacularJoinedTable])
          case "SpectacularLeftTable"   => payload.as[WsMsg.Out.Riichi.SpectacularLeftTable](deriveDecoder[WsMsg.Out.Riichi.SpectacularLeftTable])
          case "PlayerJoinedTable"      => payload.as[WsMsg.Out.Riichi.PlayerJoinedTable](deriveDecoder[WsMsg.Out.Riichi.PlayerJoinedTable])
          case "PlayerLeftTable"        => payload.as[WsMsg.Out.Riichi.PlayerLeftTable](deriveDecoder[WsMsg.Out.Riichi.PlayerLeftTable])
          case "RiichiState"             => payload.as[WsMsg.Out.Riichi.RiichiState](deriveDecoder[WsMsg.Out.Riichi.RiichiState])
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
      val json: Json = msg match {
        case x: WsMsg.In.Ping             => wrap("Ping", x)(deriveEncoder[WsMsg.In.Ping])
        case x: RiichiCmd.StartGame       => wrap("StartGame", x)(deriveEncoder[RiichiCmd.StartGame])
        case x: RiichiCmd.PauseGame       => wrap("PauseGame", x)(deriveEncoder[RiichiCmd.PauseGame])
        case x: UserCmd.GetState          => wrap("GetState", x)(deriveEncoder[UserCmd.GetState])
        case x: UserCmd.JoinAsPlayer      => wrap("JoinAsPlayer", x)(deriveEncoder[UserCmd.JoinAsPlayer])
        case x: UserCmd.LeftAsPlayer      => wrap("LeftAsPlayer", x)(deriveEncoder[UserCmd.LeftAsPlayer])
        case x: UserCmd.JoinAsSpectacular => wrap("JoinAsSpectacular", x)(deriveEncoder[UserCmd.JoinAsSpectacular])
        case x: UserCmd.LeftAsSpectacular => wrap("LeftAsSpectacular", x)(deriveEncoder[UserCmd.LeftAsSpectacular])
        case x: RiichiCmd.DiscardTile     => wrap("DiscardTile", x)(deriveEncoder[RiichiCmd.DiscardTile])
        case x: RiichiCmd.GetTileFromWall => wrap("GetTileFromWall", x)(deriveEncoder[RiichiCmd.GetTileFromWall])
        case x: RiichiCmd.ClaimTile       => wrap("ClaimTile", x)(deriveEncoder[RiichiCmd.ClaimTile])
        case x: RiichiCmd.DeclareWin      => wrap("DeclareWin", x)(deriveEncoder[RiichiCmd.DeclareWin])
      }
      json.noSpaces
    }
  }

  private def wrap[T](name: String, msg: T)(implicit encoder: ObjectEncoder[T]) = Json.obj(
    "type" -> name.asJson,
    "payload" -> msg.asJson)
}
