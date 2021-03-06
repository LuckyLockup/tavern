package com.ll.domain.ws

import com.ll.domain.auth.User
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.HumanPlayer
import com.ll.domain.games.{GameId, GameType, Player, TableId}
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.games.riichi.result.{GameScore, TablePoints}
import com.ll.domain.json.CaseClassCodec
import com.ll.domain.ws.WsMsgIn.WsRiichiCmd
import com.ll.domain.ws.WsMsgOut.Riichi.GameScored.{decoder, encoder}
import com.ll.domain.ws.WsMsgOut.Riichi.TileClaimed.{decoder, encoder}
import com.ll.domain.ws.WsRiichi.{RiichiPlayerState, WsDeclaredSet}
import io.circe.{Decoder, Encoder}
import io.circe.syntax._

sealed trait WsMsgOut

object WsMsgOut {
  sealed trait Table extends WsMsgOut { def tableId: TableId}

  case class Pong(id: Int) extends WsMsgOut

  object Pong extends CaseClassCodec {
    implicit lazy val PongEncoder: Encoder[Pong] = encoder[Pong]("Pong")
    implicit lazy val PongDecoder: Decoder[Pong] = decoder[Pong]("Pong")
  }

  case class Message(txt: String) extends WsMsgOut

  case class SpectacularJoinedTable(user: User, tableId: TableId) extends WsMsgOut

  object SpectacularJoinedTable extends CaseClassCodec {
    implicit lazy val SpectacularJoinedTableEncoder: Encoder[SpectacularJoinedTable] =
      encoder[SpectacularJoinedTable]("SpectacularJoinedTable")
    implicit lazy val SpectacularJoinedTableDecoder: Decoder[SpectacularJoinedTable] =
      decoder[SpectacularJoinedTable]("SpectacularJoinedTable")
  }

  case class SpectacularLeftTable(user: User, tableId: TableId) extends WsMsgOut

  object SpectacularLeftTable extends CaseClassCodec {
    implicit lazy val SpectacularLeftTableEncoder: Encoder[SpectacularLeftTable] =
      encoder[SpectacularLeftTable]("SpectacularLeftTable")
    implicit lazy val SpectacularLeftTableDecoder: Decoder[SpectacularLeftTable] =
      decoder[SpectacularLeftTable]("SpectacularLeftTable")
  }


  object Message extends CaseClassCodec {
    implicit lazy val MessageEncoder: Encoder[Message] = encoder[Message]("Message")
    implicit lazy val MessageDecoder: Decoder[Message] = decoder[Message]("Message")
  }

  case class ValidationError(reason: String) extends WsMsgOut

  object ValidationError extends CaseClassCodec {
    implicit lazy val WsMsgOutEncoder: Encoder[ValidationError] = encoder[ValidationError]("ValidationError")
    implicit lazy val WsMsgOutDecoder: Decoder[ValidationError] = decoder[ValidationError]("ValidationError")
  }

  sealed trait GameEvent[GT<: GameType] extends Table {
    def turn: Int
  }
  sealed trait TableState[GT<: GameType] extends Table

  object Riichi {
    sealed trait RiichiTableState extends TableState[Riichi]
    sealed trait RiichiGameEvent extends GameEvent[Riichi]

    case class RiichiState(
      tableId: TableId,
      admin: User,
      states: List[RiichiPlayerState],
      uraDoras: List[String],
      deck: Int,
      turn: Int,
      points: TablePoints
    ) extends RiichiTableState

    object RiichiState extends CaseClassCodec {
      implicit lazy val RiichiStateEncoder: Encoder[RiichiState] = encoder[RiichiState]("RiichiState")
      implicit lazy val RiichiStateDecoder: Decoder[RiichiState] = decoder[RiichiState]("RiichiState")
    }

    case class GameStarted(tableId: TableId, gameId: GameId, turn: Int) extends RiichiGameEvent

    object GameStarted extends CaseClassCodec {
      implicit lazy val GameStartedEncoder: Encoder[GameStarted] = encoder[GameStarted]("GameStarted")
      implicit lazy val GameStartedDecoder: Decoder[GameStarted] = decoder[GameStarted]("GameStarted")
    }

    case class GamePaused(tableId: TableId, gameId: GameId, turn: Int) extends RiichiGameEvent

    object GamePaused extends CaseClassCodec {
      implicit lazy val GamePausedEncoder: Encoder[GamePaused] = encoder[GamePaused]("GamePaused")
      implicit lazy val GamePausedDecoder: Decoder[GamePaused] = decoder[GamePaused]("GamePaused")
    }

    case class PlayerJoinedTable(tableId: TableId, user: Player[Riichi]) extends RiichiGameEvent {
      def turn = 0
    }

    object PlayerJoinedTable extends CaseClassCodec {
      implicit lazy val PlayerJoinedTableEncoder: Encoder[PlayerJoinedTable] =
        encoder[PlayerJoinedTable]("PlayerJoinedTable")
      implicit lazy val PlayerJoinedTableDecoder: Decoder[PlayerJoinedTable] =
        decoder[PlayerJoinedTable]("PlayerJoinedTable")
    }

    case class PlayerLeftTable(tableId: TableId, user: Player[Riichi]) extends RiichiGameEvent {
      def turn = 0
    }

    object PlayerLeftTable extends CaseClassCodec {
      implicit lazy val PlayerLeftTableEncoder: Encoder[PlayerLeftTable] = encoder[PlayerLeftTable]("PlayerLeftTable")
      implicit lazy val PlayerLeftTableDecoder: Decoder[PlayerLeftTable] = decoder[PlayerLeftTable]("PlayerLeftTable")
    }

    case class TileFromWallTaken(
      tableId: TableId,
      gameId: GameId,
      turn: Int,
      position: PlayerPosition[Riichi],
      tile: String,
      commands: List[WsRiichiCmd]
    ) extends RiichiGameEvent

    object TileFromWallTaken extends CaseClassCodec {
      implicit lazy val TileFromWallTakenEncoder: Encoder[TileFromWallTaken] =
        encoder[TileFromWallTaken]("TileFromWallTaken")
      implicit lazy val TileFromWallTakenDecoder: Decoder[TileFromWallTaken] =
        decoder[TileFromWallTaken]("TileFromWallTaken")
    }

    case class TileDiscarded(
      tableId: TableId,
      gameId: GameId,
      turn: Int,
      position: PlayerPosition[Riichi],
      tile: String,
      commands: List[WsRiichiCmd]
    ) extends RiichiGameEvent

    object TileDiscarded extends CaseClassCodec {
      implicit lazy val TileDiscardedEncoder: Encoder[TileDiscarded] = encoder[TileDiscarded]("TileDiscarded")
      implicit lazy val TileDiscardedDecoder: Decoder[TileDiscarded] = decoder[TileDiscarded]("TileDiscarded")
    }

    case class TsumoDeclared(
      tableId: TableId,
      gameId: GameId,
      turn: Int,
      position: PlayerPosition[Riichi]
    ) extends RiichiGameEvent

    object TsumoDeclared extends CaseClassCodec {
      implicit lazy val TsumoDeclaredEncoder: Encoder[TsumoDeclared] = encoder[TsumoDeclared]("TsumoDeclared")
      implicit lazy val TsumoDeclaredDecoder: Decoder[TsumoDeclared] = decoder[TsumoDeclared]("TsumoDeclared")
    }

    case class DrawDeclared(
      tableId: TableId,
      gameId: GameId,
      turn: Int
    ) extends RiichiGameEvent

    object DrawDeclared extends CaseClassCodec {
      implicit lazy val DrawDeclaredEncoder: Encoder[DrawDeclared] = encoder[DrawDeclared]("DrawDeclared")
      implicit lazy val DrawDeclaredDecoder: Decoder[DrawDeclared] = decoder[DrawDeclared]("DrawDeclared")
    }

    case class RonDeclared(
      tableId: TableId,
      gameId: GameId,
      turn: Int,
      position: PlayerPosition[Riichi]
    ) extends RiichiGameEvent

    object RonDeclared extends CaseClassCodec {
      implicit lazy val RonDeclaredEncoder: Encoder[RonDeclared] = encoder[RonDeclared]("RonDeclared")
      implicit lazy val RonDeclaredDecoder: Decoder[RonDeclared] = decoder[RonDeclared]("RonDeclared")
    }

    case class GameScored(
      tableId: TableId,
      gameId: GameId,
      turn: Int,
      score: GameScore,
    ) extends RiichiGameEvent

    object GameScored extends CaseClassCodec {
      implicit lazy val GameScoredEncoder: Encoder[GameScored] = encoder[GameScored]("GameScored")
      implicit lazy val GameScoredDecoder: Decoder[GameScored] = decoder[GameScored]("GameScored")
    }

    case class TileClaimed(
      tableId: TableId,
      gameId: GameId,
      setName: String,
      set: WsDeclaredSet,
      position: PlayerPosition[Riichi]
    ) extends RiichiGameEvent {
      def turn = set.turn
    }

    object TileClaimed extends CaseClassCodec {
      implicit lazy val TileClaimedEncoder: Encoder[TileClaimed] = encoder[TileClaimed]("TileClaimed")
      implicit lazy val TileClaimedDecoder: Decoder[TileClaimed] = decoder[TileClaimed]("TileClaimed")
    }

    case class ActionSkipped(
      tableId: TableId,
      gameId: GameId,
      turn: Int
    ) extends RiichiGameEvent

    object ActionSkipped extends CaseClassCodec {
      implicit lazy val ActionSkippedEncoder: Encoder[ActionSkipped] = encoder[ActionSkipped]("ActionSkipped")
      implicit lazy val ActionSkippedDecoder: Decoder[ActionSkipped] = decoder[ActionSkipped]("ActionSkipped")
    }

    implicit lazy val RiichiGameEventEncoder: Encoder[RiichiGameEvent] = Encoder.instance {
      case c: GameStarted => c.asJson
      case c: GamePaused => c.asJson
      case c: PlayerJoinedTable => c.asJson
      case c: PlayerLeftTable => c.asJson
      case c: TileFromWallTaken => c.asJson
      case c: TileDiscarded => c.asJson
      case c: GameScored => c.asJson
      case c: TileClaimed => c.asJson
      case c: ActionSkipped => c.asJson
      case c: TsumoDeclared => c.asJson
      case c: RonDeclared => c.asJson
      case c: DrawDeclared => c.asJson
    }

    implicit lazy val RiichiGameEventDecoder: Decoder[RiichiGameEvent] = Decoder.instance { cur =>
      import cats.syntax.either._
      GameStarted.GameStartedDecoder.apply(cur) orElse
      GamePaused.GamePausedDecoder.apply(cur) orElse
      PlayerJoinedTable.PlayerJoinedTableDecoder.apply(cur) orElse
      PlayerLeftTable.PlayerLeftTableDecoder.apply(cur) orElse
      TileFromWallTaken.TileFromWallTakenDecoder.apply(cur) orElse
      TileDiscarded.TileDiscardedDecoder.apply(cur) orElse
      TsumoDeclared.TsumoDeclaredDecoder.apply(cur) orElse
      RonDeclared.RonDeclaredDecoder.apply(cur) orElse
      GameScored.GameScoredDecoder.apply(cur) orElse
      TileClaimed.TileClaimedDecoder.apply(cur) orElse
      ActionSkipped.ActionSkippedDecoder.apply(cur) orElse
      DrawDeclared.DrawDeclaredDecoder.apply(cur)
    }
  }

  implicit lazy val WsMsgOutEncoder: Encoder[WsMsgOut] = Encoder.instance {
    case c: Riichi.RiichiGameEvent => c.asJson
    case c: Riichi.RiichiState => c.asJson
    case c: Pong => c.asJson
    case c: Message => c.asJson
    case c: ValidationError => c.asJson
    case c: SpectacularJoinedTable => c.asJson
    case c: SpectacularLeftTable => c.asJson
  }

  implicit lazy val RiichiGameEventDecoder: Decoder[WsMsgOut] = Decoder.instance { cur =>
    import cats.syntax.either._
    Riichi.RiichiGameEventDecoder.apply(cur) orElse
      Riichi.RiichiState.RiichiStateDecoder.apply(cur) orElse
      Pong.PongDecoder.apply(cur) orElse
      Message.MessageDecoder.apply(cur) orElse
      ValidationError.WsMsgOutDecoder.apply(cur) orElse
      SpectacularJoinedTable.SpectacularJoinedTableDecoder.apply(cur) orElse
      SpectacularLeftTable.SpectacularLeftTableDecoder.apply(cur)

  }
}
