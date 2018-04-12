package com.ll.domain.games.riichi

import com.ll.domain.auth.User
import com.ll.domain.games.Player.HumanPlayer
import com.ll.domain.games.PlayerPosition

sealed trait RiichiPosition extends PlayerPosition

object RiichiPosition {
  case object EastPosition extends RiichiPosition
  case object SouthPosition extends RiichiPosition
  case object WestPosition extends RiichiPosition
  case object NorthPosition extends RiichiPosition

  def addUser(humans: Set[HumanPlayer], user: User): (Set[HumanPlayer], HumanPlayer) = {
    humans.map(_.position) match {
      case positions if !positions.contains(EastPosition)  =>
        val player = HumanPlayer(user, EastPosition)
        (humans + player, player)
      case positions if !positions.contains(SouthPosition) =>
        val player = HumanPlayer(user, SouthPosition)
        (humans + player, player)
      case positions if !positions.contains(WestPosition)  =>
        val player = HumanPlayer(user, WestPosition)
        (humans + player, player)
      case positions if !positions.contains(NorthPosition) =>
        val player = HumanPlayer(user, NorthPosition)
        (humans + player, player)
      case positions                                       => (humans, HumanPlayer(user, EastPosition))
    }
  }

  def removeUser(humans: Set[HumanPlayer], user: User): Set[HumanPlayer] = {
    humans.find(_.user.id == user.id) match {
      case Some(player) => humans - player
      case None         => humans
    }
  }
}
