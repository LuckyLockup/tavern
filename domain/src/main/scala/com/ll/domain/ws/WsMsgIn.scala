package com.ll.domain.ws

import com.ll.domain.games.{GameId, GameType, TableId}
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.riichi.RiichiConfig
import com.ll.domain.games.riichi.result.HandValue
import com.ll.domain.json.CaseClassCodec
import io.circe.{Decoder, Encoder}
import io.circe.syntax._

sealed trait WsMsgIn

/**
  * These commands come from WS or from AI service with some additional information. Commands can be divided
  * into the following groups
  *  - Ping message which doesn't reach the actor
  *  - GetState message which has common logic for all games
  *  - JoinLeftCmd also generalizes joining and leaving game by players
  *  - SpectacularCmd is used inside TableActor but doesn't affect internal game state
  *  - WsGameCmd - are general game commands like Start, Stop game which are not related to any given player
  */
object WsMsgIn {
  case class Ping(id: Int) extends WsMsgIn
  object Ping extends CaseClassCodec {
    implicit lazy val PingEncoder: Encoder[Ping] = encoder[Ping]("Ping")
    implicit lazy val PingDecoder: Decoder[Ping] = decoder[Ping]("Ping")
  }

  sealed trait WsTableCmd extends WsMsgIn {def tableId: TableId}

  sealed trait SpectacularCmd extends WsTableCmd

  sealed trait WsGameCmd[GT <: GameType] extends WsTableCmd

  //intermediate trait to overcome type erasure
  sealed trait WsRiichiCmd extends WsGameCmd[Riichi]

  object SpectacularCmd {
    case class JoinAsSpectacular(tableId: TableId) extends SpectacularCmd

    object JoinAsSpectacular extends CaseClassCodec {
      implicit lazy val JoinAsSpectacularEncoder: Encoder[JoinAsSpectacular] = encoder[JoinAsSpectacular]("JoinAsSpectacular")
      implicit lazy val JoinAsSpectacularDecoder: Decoder[JoinAsSpectacular] = decoder[JoinAsSpectacular]("JoinAsSpectacular")
    }

    case class LeftAsSpectacular(tableId: TableId) extends SpectacularCmd

    object LeftAsSpectacular extends CaseClassCodec {
      implicit lazy val LeftAsSpectacularEncoder: Encoder[LeftAsSpectacular] = encoder[LeftAsSpectacular]("LeftAsSpectacular")
      implicit lazy val LeftAsSpectacularDecoder: Decoder[LeftAsSpectacular] = decoder[LeftAsSpectacular]("LeftAsSpectacular")
    }
    implicit lazy val SpectacularCmdEncoder: Encoder[SpectacularCmd] = Encoder.instance {
      case c: JoinAsSpectacular => c.asJson
      case c: LeftAsSpectacular => c.asJson
    }

    implicit lazy val SpectacularCmdDecoder: Decoder[SpectacularCmd] = Decoder.instance { cur =>
      import cats.syntax.either._
      JoinAsSpectacular.JoinAsSpectacularDecoder.apply(cur) orElse
        LeftAsSpectacular.LeftAsSpectacularDecoder.apply(cur)
    }
  }

  object WsRiichiCmd {
    case class GetState(tableId: TableId) extends WsRiichiCmd

    object GetState extends CaseClassCodec {
      implicit lazy val GetStateEncoder: Encoder[GetState] = encoder[GetState]("GetState")
      implicit lazy val GetStateDecoder: Decoder[GetState] = decoder[GetState]("GetState")
    }

    case class JoinAsPlayer(tableId: TableId) extends WsRiichiCmd

    object JoinAsPlayer extends CaseClassCodec {
      implicit lazy val JoinAsPlayerEncoder: Encoder[JoinAsPlayer] = encoder[JoinAsPlayer]("JoinAsPlayer")
      implicit lazy val JoinAsPlayerDecoder: Decoder[JoinAsPlayer] = decoder[JoinAsPlayer]("JoinAsPlayer")
    }

    case class LeftAsPlayer(tableId: TableId) extends WsRiichiCmd

    object LeftAsPlayer extends CaseClassCodec {
      implicit lazy val LeftAsPlayerEncoder: Encoder[LeftAsPlayer] = encoder[LeftAsPlayer]("LeftAsPlayer")
      implicit lazy val LeftAsPlayerDecoder: Decoder[LeftAsPlayer] = decoder[LeftAsPlayer]("LeftAsPlayer")
    }

    case class StartWsGame(tableId: TableId, gameId: GameId, config: Option[RiichiConfig] = None) extends WsRiichiCmd

    object StartWsGame extends CaseClassCodec {
      implicit lazy val StartGameEncoder: Encoder[StartWsGame] = encoder[StartWsGame]("StartGame")
      implicit lazy val StartGameDecoder: Decoder[StartWsGame] = decoder[StartWsGame]("StartGame")
    }

    case class PauseWsGame(tableId: TableId, gameId: GameId) extends WsRiichiCmd

    object PauseWsGame extends CaseClassCodec {
      implicit lazy val PauseGameEncoder: Encoder[PauseWsGame] = encoder[PauseWsGame]("PauseGame")
      implicit lazy val PauseGameDecoder: Decoder[PauseWsGame] = decoder[PauseWsGame]("PauseGame")
    }

    case class SkipAction(tableId: TableId, gameId: GameId, turn: Int) extends WsRiichiCmd

    object SkipAction extends CaseClassCodec {
      implicit lazy val SkipActionEncoder: Encoder[SkipAction] = encoder[SkipAction]("SkipAction")
      implicit lazy val SkipActionDecoder: Decoder[SkipAction] = decoder[SkipAction]("SkipAction")
    }

    case class DiscardTile(
      tableId: TableId,
      gameId: GameId,
      tile: String,
      turn: Int) extends WsRiichiCmd

    object DiscardTile extends CaseClassCodec {
      implicit lazy val DiscardTileEncoder: Encoder[DiscardTile] = encoder[DiscardTile]("DiscardTile")
      implicit lazy val DiscardTileDecoder: Decoder[DiscardTile] = decoder[DiscardTile]("DiscardTile")
    }

    case class GetTileFromWall(
      tableId: TableId,
      gameId: GameId,
      turn: Int
    ) extends WsRiichiCmd

    object GetTileFromWall extends CaseClassCodec {
      implicit lazy val GetTileFromWallEncoder: Encoder[GetTileFromWall] = encoder[GetTileFromWall]("GetTileFromWall")
      implicit lazy val GetTileFromWallDecoder: Decoder[GetTileFromWall] = decoder[GetTileFromWall]("GetTileFromWall")
    }

    case class ClaimPung(
      tableId: TableId,
      gameId: GameId,
      turn: Int
    ) extends WsRiichiCmd

    object ClaimPung extends CaseClassCodec {
      implicit lazy val ClaimPungEncoder: Encoder[ClaimPung] = encoder[ClaimPung]("ClaimPung")
      implicit lazy val ClaimPungDecoder: Decoder[ClaimPung] = decoder[ClaimPung]("ClaimPung")
    }

    case class ClaimChow(
      tableId: TableId,
      gameId: GameId,
      turn: Int,
      tile: String,
      tiles: List[String]
    ) extends WsRiichiCmd

    object ClaimChow extends CaseClassCodec {
      implicit lazy val ClaimChowEncoder: Encoder[ClaimChow] = encoder[ClaimChow]("ClaimChow")
      implicit lazy val ClaimChowDecoder: Decoder[ClaimChow] = decoder[ClaimChow]("ClaimChow")
    }

    case class DeclareRon(
      tableId: TableId,
      gameId: GameId,
      turn: Int,
      approximateHandValue: Option[HandValue]
    ) extends WsRiichiCmd

    object DeclareRon extends CaseClassCodec {
      implicit lazy val DeclareRonEncoder: Encoder[DeclareRon] = encoder[DeclareRon]("DeclareRon")
      implicit lazy val DeclareRonDecoder: Decoder[DeclareRon] = decoder[DeclareRon]("DeclareRon")
    }

    case class DeclareTsumo(
      tableId: TableId,
      gameId: GameId,
      turn: Int,
      approxHandValue: Option[HandValue]
    ) extends WsRiichiCmd

    object DeclareTsumo extends CaseClassCodec {
      implicit lazy val DeclareTsumoEncoder: Encoder[DeclareTsumo] = encoder[DeclareTsumo]("DeclareTsumo")
      implicit lazy val DeclareTsumoDecoder: Decoder[DeclareTsumo] = decoder[DeclareTsumo]("DeclareTsumo")
    }

    implicit lazy val WsRiichiCmdEncoder: Encoder[WsRiichiCmd] = Encoder.instance {
      case c: DiscardTile     => c.asJson
      case c: GetTileFromWall => c.asJson
      case c: ClaimPung       => c.asJson
      case c: ClaimChow       => c.asJson
      case c: DeclareRon      => c.asJson
      case c: DeclareTsumo    => c.asJson
      case c: StartWsGame     => c.asJson
      case c: PauseWsGame     => c.asJson
      case c: JoinAsPlayer    => c.asJson
      case c: LeftAsPlayer    => c.asJson
      case c: GetState        => c.asJson
      case c: SkipAction      => c.asJson
    }

    implicit lazy val WsRiichiCmdDecoder: Decoder[WsRiichiCmd] = Decoder.instance { cur =>
      import cats.syntax.either._
      JoinAsPlayer.JoinAsPlayerDecoder.apply(cur) orElse
      LeftAsPlayer.LeftAsPlayerDecoder.apply(cur) orElse
      GetState.GetStateDecoder.apply(cur) orElse
      DiscardTile.DiscardTileDecoder.apply(cur) orElse
      GetTileFromWall.GetTileFromWallDecoder.apply(cur) orElse
      ClaimPung.ClaimPungDecoder.apply(cur) orElse
      ClaimChow.ClaimChowDecoder.apply(cur) orElse
      DeclareRon.DeclareRonDecoder.apply(cur) orElse
      DeclareTsumo.DeclareTsumoDecoder.apply(cur) orElse
      StartWsGame.StartGameDecoder.apply(cur) orElse
      PauseWsGame.PauseGameDecoder.apply(cur) orElse
      SkipAction.SkipActionDecoder.apply(cur)
    }
  }

  implicit lazy val WsMsgInEncoder: Encoder[WsMsgIn] = Encoder.instance {
    case c: Ping           => c.asJson
    case c: WsRiichiCmd    => c.asJson
    case c: SpectacularCmd => c.asJson
  }

  implicit lazy val WsMsgInDecoder: Decoder[WsMsgIn] = Decoder.instance { cur =>
    import cats.syntax.either._
    Ping.PingDecoder.apply(cur) orElse
      WsRiichiCmd.WsRiichiCmdDecoder.apply(cur) orElse
      SpectacularCmd.SpectacularCmdDecoder.apply(cur)
  }
}