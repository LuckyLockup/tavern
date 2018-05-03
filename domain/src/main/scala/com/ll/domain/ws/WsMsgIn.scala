package com.ll.domain.ws

import com.ll.domain.auth.User
import com.ll.domain.games.{GameId, GameType, TableId}
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.riichi.RiichiConfig
import com.ll.domain.games.riichi.result.HandValue
import com.ll.domain.json.CaseClassCodec
import io.circe.{Decoder, Encoder}
import io.circe.syntax._


sealed trait WsMsgIn

object WsMsgIn {
  case class Ping(id: Int) extends WsMsgIn

  object Ping extends CaseClassCodec {
    implicit lazy val PingEncoder: Encoder[Ping] = encoder[Ping]("Ping")
    implicit lazy val PingDecoder: Decoder[Ping] = decoder[Ping]("Ping")
  }

  sealed trait CommonCmd extends WsMsgIn {def tableId: TableId}

  sealed trait GameCmd[GT <: GameType] extends WsMsgIn {
    def tableId: TableId
    def gameId: GameId
  }


  object CommonCmd {
    case class GetState(tableId: TableId) extends CommonCmd

    object GetState extends CaseClassCodec {
      implicit lazy val GetStateEncoder: Encoder[GetState] = encoder[GetState]("GetState")
      implicit lazy val GetStateDecoder: Decoder[GetState] = decoder[GetState]("GetState")
    }

    case class JoinAsSpectacular(tableId: TableId) extends CommonCmd

    object JoinAsSpectacular extends CaseClassCodec {
      implicit lazy val JoinAsSpectacularEncoder: Encoder[JoinAsSpectacular] = encoder[JoinAsSpectacular]("JoinAsSpectacular")
      implicit lazy val JoinAsSpectacularDecoder: Decoder[JoinAsSpectacular] = decoder[JoinAsSpectacular]("JoinAsSpectacular")
    }

    case class LeftAsSpectacular(tableId: TableId) extends CommonCmd

    object LeftAsSpectacular extends CaseClassCodec {
      implicit lazy val LeftAsSpectacularEncoder: Encoder[LeftAsSpectacular] = encoder[LeftAsSpectacular]("LeftAsSpectacular")
      implicit lazy val LeftAsSpectacularDecoder: Decoder[LeftAsSpectacular] = decoder[LeftAsSpectacular]("LeftAsSpectacular")
    }

    case class JoinAsPlayer(tableId: TableId) extends CommonCmd

    object JoinAsPlayer extends CaseClassCodec {
      implicit lazy val JoinAsPlayerEncoder: Encoder[JoinAsPlayer] = encoder[JoinAsPlayer]("JoinAsPlayer")
      implicit lazy val JoinAsPlayerDecoder: Decoder[JoinAsPlayer] = decoder[JoinAsPlayer]("JoinAsPlayer")
    }

    case class LeftAsPlayer(tableId: TableId) extends CommonCmd

    object LeftAsPlayer extends CaseClassCodec {
      implicit lazy val LeftAsPlayerEncoder: Encoder[LeftAsPlayer] = encoder[LeftAsPlayer]("LeftAsPlayer")
      implicit lazy val LeftAsPlayerDecoder: Decoder[LeftAsPlayer] = decoder[LeftAsPlayer]("LeftAsPlayer")
    }

    implicit lazy val TableCmdEncoder: Encoder[CommonCmd] = Encoder.instance {
      case c: GetState => c.asJson
      case c: JoinAsSpectacular => c.asJson
      case c: LeftAsSpectacular => c.asJson
      case c: JoinAsPlayer => c.asJson
      case c: LeftAsPlayer => c.asJson
    }

    implicit lazy val TableCmdDecoder: Decoder[CommonCmd] = Decoder.instance { cur =>
      import cats.syntax.either._
      GetState.GetStateDecoder.apply(cur) orElse
        JoinAsSpectacular.JoinAsSpectacularDecoder.apply(cur) orElse
        LeftAsSpectacular.LeftAsSpectacularDecoder.apply(cur) orElse
        JoinAsPlayer.JoinAsPlayerDecoder.apply(cur) orElse
        LeftAsPlayer.LeftAsPlayerDecoder.apply(cur)
    }
  }

  object RiichiGameCmd {
    //intermediate trait to overcome type erasure
    sealed trait RiichiCmd extends GameCmd[Riichi]

    case class StartGame(tableId: TableId, gameId: GameId, config: RiichiConfig) extends RiichiCmd

    object StartGame extends CaseClassCodec {
      implicit lazy val StartGameEncoder: Encoder[StartGame] = encoder[StartGame]("StartGame")
      implicit lazy val StartGameDecoder: Decoder[StartGame] = decoder[StartGame]("StartGame")
    }

    case class PauseGame(tableId: TableId, gameId: GameId) extends RiichiCmd

    object PauseGame extends CaseClassCodec {
      implicit lazy val PauseGameEncoder: Encoder[PauseGame] = encoder[PauseGame]("PauseGame")
      implicit lazy val PauseGameDecoder: Decoder[PauseGame] = decoder[PauseGame]("PauseGame")
    }

    case class DiscardTile(
      tableId: TableId,
      gameId: GameId,
      tile: String,
      turn: Int) extends RiichiCmd

    object DiscardTile extends CaseClassCodec {
      implicit lazy val DiscardTileEncoder: Encoder[DiscardTile] = encoder[DiscardTile]("DiscardTile")
      implicit lazy val DiscardTileDecoder: Decoder[DiscardTile] = decoder[DiscardTile]("DiscardTile")
    }

    case class GetTileFromWall(
      tableId: TableId,
      gameId: GameId,
      turn: Int) extends RiichiCmd

    object GetTileFromWall extends CaseClassCodec {
      implicit lazy val GetTileFromWallEncoder: Encoder[GetTileFromWall] = encoder[GetTileFromWall]("GetTileFromWall")
      implicit lazy val GetTileFromWallDecoder: Decoder[GetTileFromWall] = decoder[GetTileFromWall]("GetTileFromWall")
    }

    case class ClaimPung(
      tableId: TableId,
      gameId: GameId,
      from: PlayerPosition[Riichi],
      turn: Int,
      tiles: List[String]) extends RiichiCmd

    object ClaimPung extends CaseClassCodec {
      implicit lazy val ClaimPungEncoder: Encoder[ClaimPung] = encoder[ClaimPung]("ClaimPung")
      implicit lazy val ClaimPungDecoder: Decoder[ClaimPung] = decoder[ClaimPung]("ClaimPung")
    }

    case class ClaimChow(
      tableId: TableId,
      gameId: GameId,
      from: PlayerPosition[Riichi],
      tiles: List[String]
    ) extends RiichiCmd

    object ClaimChow extends CaseClassCodec {
      implicit lazy val ClaimChowEncoder: Encoder[ClaimChow] = encoder[ClaimChow]("ClaimChow")
      implicit lazy val ClaimChowDecoder: Decoder[ClaimChow] = decoder[ClaimChow]("ClaimChow")
    }

    case class DeclareRon(
      tableId: TableId,
      gameId: GameId,
      approximateHandValue: HandValue
    ) extends RiichiCmd

    object DeclareRon extends CaseClassCodec {
      implicit lazy val DeclareRonEncoder: Encoder[DeclareRon] = encoder[DeclareRon]("DeclareRon")
      implicit lazy val DeclareRonDecoder: Decoder[DeclareRon] = decoder[DeclareRon]("DeclareRon")
    }

    case class DeclareTsumo(
      tableId: TableId,
      gameId: GameId,
      approxHandValue: Option[HandValue]
    ) extends RiichiCmd

    object DeclareTsumo extends CaseClassCodec {
      implicit lazy val DeclareTsumoEncoder: Encoder[DeclareTsumo] = encoder[DeclareTsumo]("DeclareTsumo")
      implicit lazy val DeclareTsumoDecoder: Decoder[DeclareTsumo] = decoder[DeclareTsumo]("DeclareTsumo")
    }

    case class ScoreGame(
      tableId: TableId,
      gameId: GameId
    ) extends RiichiCmd

    object ScoreGame extends CaseClassCodec {
      implicit lazy val ScoreGameEncoder: Encoder[ScoreGame] = encoder[ScoreGame]("ScoreGame")
      implicit lazy val ScoreGameDecoder: Decoder[ScoreGame] = decoder[ScoreGame]("ScoreGame")
    }

    implicit lazy val RiichiGameCmdEncoder: Encoder[RiichiCmd] = Encoder.instance {
      case c: StartGame => c.asJson
      case c: PauseGame => c.asJson
      case c: DiscardTile => c.asJson
      case c: GetTileFromWall => c.asJson
      case c: ClaimPung => c.asJson
      case c: ClaimChow => c.asJson
      case c: DeclareRon => c.asJson
      case c: DeclareTsumo => c.asJson
      case c: ScoreGame => c.asJson
    }

    implicit lazy val RiichiGameCmdDecoder: Decoder[RiichiCmd] = Decoder.instance { cur =>
      import cats.syntax.either._
      StartGame.StartGameDecoder.apply(cur)  orElse
      PauseGame.PauseGameDecoder.apply(cur) orElse
      DiscardTile.DiscardTileDecoder.apply(cur) orElse
      GetTileFromWall.GetTileFromWallDecoder.apply(cur) orElse
      ClaimPung.ClaimPungDecoder.apply(cur) orElse
      ClaimChow.ClaimChowDecoder.apply(cur) orElse
      DeclareRon.DeclareRonDecoder.apply(cur) orElse
      DeclareTsumo.DeclareTsumoDecoder.apply(cur) orElse
      ScoreGame.ScoreGameDecoder.apply(cur)
    }
  }

  implicit lazy val WsMsgInEncoder: Encoder[WsMsgIn] = Encoder.instance {
    case c: Ping                    => c.asJson
    case c: RiichiGameCmd.RiichiCmd => c.asJson
    case c: CommonCmd               => c.asJson
  }

  implicit lazy val WsMsgInDecoder: Decoder[WsMsgIn] = Decoder.instance { cur =>
    import cats.syntax.either._
    Ping.PingDecoder.apply(cur) orElse
    RiichiGameCmd.RiichiGameCmdDecoder.apply(cur) orElse
    CommonCmd.TableCmdDecoder.apply(cur)
  }
}