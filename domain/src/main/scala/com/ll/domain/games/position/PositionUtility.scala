package com.ll.domain.games.position

import com.ll.domain.auth.User
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player
import com.ll.domain.games.Player.HumanPlayer
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.ws.WsMsgOut.ValidationError

object PositionUtility {
  def addUser(humans: Set[Player[Riichi]], user: User): Either[ValidationError, Player[Riichi]] = {
    humans.map(_.position) match {
      case positions if !positions.contains(RiichiPosition.EastPosition)  =>
        Right(HumanPlayer(user, RiichiPosition.EastPosition))
      case positions if !positions.contains(RiichiPosition.SouthPosition) =>
        Right(HumanPlayer(user, RiichiPosition.SouthPosition))
      case positions if !positions.contains(RiichiPosition.WestPosition)  =>
        Right(HumanPlayer(user, RiichiPosition.WestPosition))
      case positions if !positions.contains(RiichiPosition.NorthPosition) =>
        Right(HumanPlayer(user, RiichiPosition.NorthPosition))
      case _                                                      =>
       Left(ValidationError("Table is full"))
    }
  }
}
