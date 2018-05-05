package com.ll.domain.games.riichi

import com.ll.domain.auth.{User, UserId}
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.Player
import com.ll.domain.games.Player.HumanPlayer
import com.ll.domain.games.position.PlayerPosition.RiichiPosition
import com.ll.domain.games.position.PositionUtility
import org.scalatest.{Matchers, WordSpec}

class RiichiPositionTest extends WordSpec with Matchers{

  "4 players add in order" in {
    var players: Set[Player[Riichi]] = Set.empty
    val user1 = User(UserId(1), "Akagi1")
    val user2 = User(UserId(2), "Akagi2")
    val user3 = User(UserId(3), "Akagi3")
    val user4 = User(UserId(4), "Akagi4")

    players = players + PositionUtility.addUser(players, user1).right.get
    players = players + PositionUtility.addUser(players, user2).right.get
    players = players + PositionUtility.addUser(players, user3).right.get
    players = players + PositionUtility.addUser(players, user4).right.get

    players should contain (HumanPlayer(user1, RiichiPosition.EastPosition))
    players should contain (HumanPlayer(user2, RiichiPosition.SouthPosition))
    players should contain (HumanPlayer(user3, RiichiPosition.WestPosition))
    players should contain (HumanPlayer(user4, RiichiPosition.NorthPosition))
  }

  "4 players add and remove in order" in {
    var players: Set[Player[Riichi]] = Set.empty
    val user1 = User(UserId(1), "Akagi1")
    val user2 = User(UserId(2), "Akagi2")
    val user3 = User(UserId(3), "Akagi3")
    val user4 = User(UserId(4), "Akagi4")

    players = players + PositionUtility.addUser(players, user1).right.get
    players = players + PositionUtility.addUser(players, user2).right.get
    players = players + PositionUtility.addUser(players, user3).right.get
    players = players - HumanPlayer(user1, RiichiPosition.EastPosition)
    players = players + PositionUtility.addUser(players, user4).right.get
    players = players + PositionUtility.addUser(players, user1).right.get

    players should contain (HumanPlayer(user4, RiichiPosition.EastPosition))
    players should contain (HumanPlayer(user2, RiichiPosition.SouthPosition))
    players should contain (HumanPlayer(user3, RiichiPosition.WestPosition))
    players should contain (HumanPlayer(user1, RiichiPosition.NorthPosition))
  }
}
