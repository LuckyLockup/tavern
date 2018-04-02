package com.ll.domain.ws

import com.ll.domain.ValidationError
import com.ll.domain.games.persistence.{GeneralEvent, OutEvent, PlayerEvent}


object OutEventConverter {

  def convert(event: OutEvent): WsMsg.Out = event match {
    case GeneralEvent.PlayerJoinedGame(userId) =>
      WsMsg.Out.PlayerJoinedTheGame(userId)
    case st: PlayerEvent.RiichiGameState =>
      WsMsg.Out.GameState(
        gameId = st.gameId,
        closedHand = st.closedHand.map(_.repr),
        openHand = st.openHand.map(_.repr),
        currentTitle = st.currentTitle.map(_.repr),
        discard = st.discard.map(_.repr)
      )
    case PlayerEvent.TileFromWall(userId, tile) =>
      WsMsg.Out.TileFromWall(tile.repr)
    case PlayerEvent.TileDiscarded(userId, tile) =>
      WsMsg.Out.TileDiscarded(tile.repr)
  }

  def convert(error: ValidationError): WsMsg.Out = WsMsg.Out.ValidationError(error.reason)
}
