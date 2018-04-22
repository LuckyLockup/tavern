package com.ll.domain.ws

import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player
import com.ll.domain.json.CaseClassCodec

object WsRiichi {
  case class RiichiPlayerState(
    player: Player[Riichi],
    closedHand: List[String],
    currentTile: Option[String] = None,
    discard: List[String] = Nil,
    online: Boolean = true
  )

  object RiichiPlayerState extends CaseClassCodec {
    implicit lazy val RiichiPlayerStateEncoder = encoder[RiichiPlayerState]("RiichiPlayerState")
    implicit lazy val RiichiPlayerStateDecoder = decoder[RiichiPlayerState]("RiichiPlayerState")

  }
}
