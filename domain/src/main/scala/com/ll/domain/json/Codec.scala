package com.ll.domain.json

import com.ll.domain.ai.AIType
import com.ll.domain.auth.User
import com.ll.domain.games.{Player, PlayerPosition, TableId}
import com.ll.domain.games.Player.{AIPlayer, HumanPlayer}
import com.ll.domain.games.riichi.RiichiPosition
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

  implicit lazy val aiTypeEncoder: Encoder[AIType] = new Encoder[AIType] {
    final def apply(a: AIType): Json = a match {
      case AIType.Duck  => "Duck".asJson
    }
  }
  implicit lazy val aiTypeDecoder: Decoder[AIType] = new Decoder[AIType] {
    final def apply(c: HCursor): Decoder.Result[AIType] = {
      def decode(str: String): Decoder.Result[AIType] = str match {
        case "Duck"  => Right(AIType.Duck)
        case _               => Left(DecodingFailure(s"$str is not known AI type", Nil))
      }

      c.focus.flatMap(_.asString).map(s => decode(s))
        .getOrElse(Left(DecodingFailure("Can't decode AI type from", Nil)))
    }
  }


  implicit lazy val userEncoder = deriveEncoder[User]
  implicit lazy val userDecoder = deriveDecoder[User]

  //Player positions
  implicit lazy val positionEncoder: Encoder[PlayerPosition] = new Encoder[PlayerPosition] {
    final def apply(a: PlayerPosition): Json = a match {
      case RiichiPosition.EastPosition  => "EastPosition".asJson
      case RiichiPosition.SouthPosition => "SouthPosition".asJson
      case RiichiPosition.WestPosition  => "WestPosition".asJson
      case RiichiPosition.NorthPosition => "NorthPosition".asJson
    }
  }

  implicit lazy val positionDecoder: Decoder[PlayerPosition] = new Decoder[PlayerPosition] {
    final def apply(c: HCursor): Decoder.Result[PlayerPosition] = {
      def decode(str: String): Decoder.Result[PlayerPosition] = str match {
        case "EastPosition"  => Right(RiichiPosition.EastPosition)
        case "SouthPosition" => Right(RiichiPosition.SouthPosition)
        case "WestPosition"  => Right(RiichiPosition.WestPosition)
        case "NorthPosition" => Right(RiichiPosition.NorthPosition)
        case _               => Left(DecodingFailure(s"$str is not known position", Nil))
      }

      c.focus.flatMap(_.asString).map(s => decode(s))
        .getOrElse(Left(DecodingFailure("Can't decode position from", Nil)))
    }
  }

  //Players
  implicit lazy val HumanPlayerDecoder = decoder[Player.HumanPlayer]("HumanPlayer")
  implicit lazy val HumanPlayerEncoder = encoder[Player.HumanPlayer]("HumanPlayer")
  implicit lazy val AiPlayerDecoder = decoder[Player.AIPlayer]("AIPlayer")
  implicit lazy val AiPlayerEncoder = encoder[Player.AIPlayer]("AIPlayer")
  implicit lazy val PlayerDeocer = deriveDecoder[Player]
  implicit lazy val playerEncoder = deriveEncoder[Player]

  implicit val PlayerEncoder: Encoder[Player] = new Encoder[Player] {
    final def apply(a: Player): Json = a match {
      case x: HumanPlayer => wrap("HumanPlayer", x)(deriveEncoder[HumanPlayer])
      case x: AIPlayer    => wrap("AIPlayer", x)(deriveEncoder[AIPlayer])
    }
  }

  def decodeWsMsg(json: String): Either[DecodingFailure, WsMsg.In] = {
    implicit val inDecoder: Decoder[WsMsg.In] = new Decoder[WsMsg.In] {
      final def apply(c: HCursor): Decoder.Result[WsMsg.In] = {
        def decode(messageType: String, payload: Json): Decoder.Result[WsMsg.In] = messageType match {
          case "Ping"              => payload.as[WsMsg.In.Ping](deriveDecoder[WsMsg.In.Ping])
          case "StartGame"         => payload.as[TableCmd.StartGame](deriveDecoder[TableCmd.StartGame])
          case "PauseGame"         => payload.as[TableCmd.PauseGame](deriveDecoder[TableCmd.PauseGame])
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
      case x: WsMsg.Out.Pong                         => wrap("Pong", x)(deriveEncoder[WsMsg.Out.Pong])
      case x: WsMsg.Out.Message                      => wrap("Message", x)(deriveEncoder[WsMsg.Out.Message])
      case x: WsMsg.Out.ValidationError              => wrap("ValidationError", x)(deriveEncoder[WsMsg.Out.ValidationError])
      case x: WsMsg.Out.Table.PlayerJoinedTable      =>
        wrap("PlayerJoinedTable", x)(deriveEncoder[WsMsg.Out.Table.PlayerJoinedTable])
      case x: WsMsg.Out.Table.PlayerLeftTable        =>
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
          case "PlayerJoinedTable"      => payload.as[WsMsg.Out.Table.PlayerJoinedTable](deriveDecoder[WsMsg.Out.Table.PlayerJoinedTable])
          case "PlayerLeftTable"        => payload.as[WsMsg.Out.Table.PlayerLeftTable](deriveDecoder[WsMsg.Out.Table.PlayerLeftTable])
          case "TableState"             => payload.as[WsMsg.Out.Table.TableState](deriveDecoder[WsMsg.Out.Table.TableState])
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
        case x: TableCmd.StartGame        => wrap("StartGame", x)(deriveEncoder[TableCmd.StartGame])
        case x: TableCmd.PauseGame        => wrap("PauseGame", x)(deriveEncoder[TableCmd.PauseGame])
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
