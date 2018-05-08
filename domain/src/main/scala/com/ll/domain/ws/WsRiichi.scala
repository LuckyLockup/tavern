package com.ll.domain.ws

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.{GameType, Player}
import com.ll.domain.games.deck.{DeclaredSet, Tile}
import com.ll.domain.games.position.PlayerPosition
import com.ll.domain.json.CaseClassCodec
import io.circe.{Decoder, Encoder}

object WsRiichi {
  case class RiichiPlayerState(
    player: Player[Riichi],
    closedHand: List[String],
    openHand: List[WsDeclaredSet] = Nil,
    currentTile: Option[String] = None,
    discard: List[String] = Nil,
    online: Boolean = true
  )

  object RiichiPlayerState extends CaseClassCodec {
    implicit lazy val RiichiPlayerStateEncoder = encoder[RiichiPlayerState]("RiichiPlayerState")
    implicit lazy val RiichiPlayerStateDecoder = decoder[RiichiPlayerState]("RiichiPlayerState")
  }

  case class WsDeclaredSet(
    claimedTile: String,
    tails: List[String],
    from: PlayerPosition[Riichi],
    turn: Int
  )

  object WsDeclaredSet extends CaseClassCodec {
    implicit lazy val WsDeclaredSetEncoder: Encoder[WsDeclaredSet] = encoder[WsDeclaredSet]("WsDeclaredSet")
    implicit lazy val WsDeclaredSetDecoder: Decoder[WsDeclaredSet] = decoder[WsDeclaredSet]("WsDeclaredSet")

    def apply(set: DeclaredSet[Riichi]): WsDeclaredSet = WsDeclaredSet(
      set.claimedTile.repr,
      set.tails.map(_.repr),
      set.from,
      set.turn
    )
  }
}
