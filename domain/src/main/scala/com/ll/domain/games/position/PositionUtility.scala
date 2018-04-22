package com.ll.domain.games.position

import com.ll.domain.auth.User
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player
import com.ll.domain.games.Player.HumanPlayer
import com.ll.domain.games.position.PlayerPosition.RiichiPosition

object PositionUtility {
  def addUser(humans: Set[Player[Riichi]], user: User): (Set[Player[Riichi]], HumanPlayer[Riichi]) = {
    humans.map(_.position) match {
      case positions if !positions.contains(RiichiPosition.EastPosition)  =>
        val player = HumanPlayer(user, RiichiPosition.EastPosition)
        (humans + player, player)
      case positions if !positions.contains(RiichiPosition.SouthPosition) =>
        val player = HumanPlayer(user, RiichiPosition.SouthPosition)
        (humans + player, player)
      case positions if !positions.contains(RiichiPosition.WestPosition)  =>
        val player = HumanPlayer(user, RiichiPosition.WestPosition)
        (humans + player, player)
      case positions if !positions.contains(RiichiPosition.NorthPosition) =>
        val player = HumanPlayer(user, RiichiPosition.NorthPosition)
        (humans + player, player)
      case positions                                                      =>
        (humans, HumanPlayer(user, RiichiPosition.EastPosition))
    }
  }
}
