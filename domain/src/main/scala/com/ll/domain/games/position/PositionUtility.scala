package com.ll.domain.games.position

import com.ll.domain.auth.User
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player.Riichi.HumanPlayer
import com.ll.domain.games.position.PlayerPosition.RiichiPosition

object PositionUtility {
  def addUser(humans: Set[HumanPlayer[Riichi]], user: User): (Set[HumanPlayer[Riichi]], HumanPlayer[Riichi]) = {
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

  def removeUser(humans: Set[HumanPlayer[Riichi]], user: User): Set[HumanPlayer[Riichi]] = {
    humans.find(p => p.user.id == user.id) match {
      case Some(player) =>
        humans - player
      case None => humans
    }
  }
}
