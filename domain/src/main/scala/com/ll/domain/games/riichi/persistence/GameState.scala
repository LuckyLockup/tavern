package com.ll.domain.games.riichi.persistence

import com.ll.domain.auth.UserId
import com.ll.domain.games.GameId
import com.ll.domain.games.persistence._
import com.ll.domain.games.riichi.{Deck, Tile}

import scala.util.Random

case class GameState(
  id: GameId,
  wall: List[Tile] = Random.shuffle(Deck.allTiles),
  hands: Map[UserId, PlayerState] = Map(),
  turn: Integer = 0
) {
  def validate(cmd: Cmd): Either[ValidationError, List[Event]] = cmd match {
    case GameCmd.StartGame(_)                    =>
      if (this.turn == 0) {
        Right(List(GameEvent.GameStarted))
      } else {
        Left(ValidationError(s"Game is already on turn $turn"))
      }

    case GameCmd.JoinGameAsPlayer(userId, _) =>
      if (this.hands.size <= 1 && !this.hands.keySet.contains(userId)) {
        Right(List(GameEvent.PlayerJoinedTable(userId), GameEvent.GameStarted, RiichiEvent.TileTaken(userId)))
      } else {
        Left(ValidationError("Can't join table"))
      }

    case RiichiCmd.DiscardTile(userId, _, tile) =>
      this.hands.get(userId)
        .flatMap { hand =>
          hand.closedHand.find(t => t.repr == tile)
        }
        .map(tile => Right(List(RiichiEvent.TileDiscarded(userId, tile))))
        .getOrElse(Left(ValidationError(s"$tile is not in discard")))

    case RiichiCmd.GetTileFromWall(userId, _) =>
      this.hands.get(userId)
        .filter(st => st.currentTitle.isEmpty && this.wall.nonEmpty)
        .map(st => Right(List(RiichiEvent.TileTaken(userId))))
        .getOrElse(Left(ValidationError("can't take tile")))
  }

  def applyEvent(event: Event): (GameState, List[OutEvent]) = event match {
    case GameEvent.PlayerJoinedTable(userId) =>
      val newState = this.copy(
        hands = hands + (userId -> PlayerState())
      )
      (newState, List(GeneralEvent.PlayerJoinedGame(userId)))

    case GameEvent.GameStarted =>
      val (hand1, remaining) = wall.splitAt(GameState.handSize)
      val newPlayerStates = this.hands.mapValues(playerState => playerState.copy(closedHand = hand1))
      val newState = this.copy(
        wall = remaining,
        hands = newPlayerStates
      )
      (newState, newState.playerProjections)

    case RiichiEvent.TileTaken(userId) =>
      val newTile = wall.head
      val remaining = wall.drop(1)

      val newHands = this.hands.map {
        case (`userId`, hand) => (userId, hand.copy(currentTitle = Some(newTile)))
        case other => other
      }

      val newState = this.copy(
        wall = remaining,
        hands = newHands
      )
      (newState, List(PlayerEvent.TileFromWall(userId, newTile)))

    case RiichiEvent.TileDiscarded(userId, tile) =>
      val newHands = this.hands.map {
        case (`userId`, hand) =>
          val newClosedHand = (hand.currentTitle.get :: hand.closedHand).filter(t => t != tile)
          val newDiscard = tile :: hand.discard
          val newHand = hand.copy(
            closedHand = newClosedHand,
            discard = newDiscard,
            currentTitle = None
          )
          (userId, newHand)
        case other => other
      }
      val newState = this.copy(
        hands = newHands
      )
      (newState, List(PlayerEvent.TileDiscarded(userId, tile)))
  }

  def playerProjections: List[PlayerEvent.RiichiGameState] = {
    this.hands.map {
      case (userId, playerState) =>
        PlayerEvent.RiichiGameState (
          userId = userId,
          gameId = this.id,
          closedHand = playerState.closedHand,
          openHand = playerState.openHand,
          currentTitle = playerState.currentTitle,
          discard = playerState.discard
        )
    }.toList
  }

  def playerProjection(userId: UserId): Option[PlayerEvent.RiichiGameState] = {
    this.hands.get(userId).map(playerState =>
      PlayerEvent.RiichiGameState (
        userId = userId,
        gameId = this.id,
        closedHand = playerState.closedHand,
        openHand = playerState.openHand,
        currentTitle = playerState.currentTitle,
        discard = playerState.discard
      )
    )
  }

  def players: Set[UserId] = hands.keySet

}

case object GameState {
  val handSize = 13
}
