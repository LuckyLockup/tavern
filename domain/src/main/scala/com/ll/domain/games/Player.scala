package com.ll.domain.games

import com.ll.domain.auth.{User, UserId}

sealed trait Player {
  def nickName: String
}

case class HumanPlayer(userId: UserId, nickName: String) extends Player

object HumanPlayer {
  def apply(user: User): HumanPlayer = new HumanPlayer(user.id, user.nickname)
}
case class AIPlayer(nickName: String) extends Player

