package com.ll.domain.games.riichi

import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.Player.Riichi.HumanPlayer
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.position.PositionUtility
import org.scalatest.{Matchers, WordSpec}

class RiichiPositionTest extends WordSpec with Matchers{

  "4 players add in order" in {
    var players: Set[HumanPlayer] = Set.empty
    val user1 = User(UserId(1), "Akagi1")
    val user2 = User(UserId(2), "Akagi2")
    val user3 = User(UserId(3), "Akagi3")
    val user4 = User(UserId(4), "Akagi4")

    players = PositionUtility.addUser(players, user1)._1
    players = PositionUtility.addUser(players, user2)._1
    players = PositionUtility.addUser(players, user3)._1
    players = PositionUtility.addUser(players, user4)._1

    players should contain (HumanPlayer(user1, RiichiPosition.EastPosition))
    players should contain (HumanPlayer(user2, RiichiPosition.SouthPosition))
    players should contain (HumanPlayer(user3, RiichiPosition.WestPosition))
    players should contain (HumanPlayer(user4, RiichiPosition.NorthPosition))
  }

  "4 players add and remove in order" in {
    var players: Set[HumanPlayer] = Set.empty
    val user1 = User(UserId(1), "Akagi1")
    val user2 = User(UserId(2), "Akagi2")
    val user3 = User(UserId(3), "Akagi3")
    val user4 = User(UserId(4), "Akagi4")

    players = PositionUtility.addUser(players, user1)._1
    players = PositionUtility.addUser(players, user2)._1
    players = PositionUtility.addUser(players, user3)._1
    players = PositionUtility.removeUser(players, user1)
    players = PositionUtility.addUser(players, user4)._1
    players = PositionUtility.addUser(players, user1)._1

    players should contain (HumanPlayer(user4, RiichiPosition.EastPosition))
    players should contain (HumanPlayer(user2, RiichiPosition.SouthPosition))
    players should contain (HumanPlayer(user3, RiichiPosition.WestPosition))
    players should contain (HumanPlayer(user1, RiichiPosition.NorthPosition))
  }
}
