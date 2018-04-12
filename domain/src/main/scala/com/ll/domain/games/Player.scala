package com.ll.domain.games

import com.ll.domain.auth.User

sealed trait Player {
  def nickName: String
  def position: PlayerPosition
}


object Player{
  case class HumanPlayer(user: User, position: PlayerPosition) extends Player {
    def nickName = user.nickname
  }
  case class AIPlayer(nickName: String, position: PlayerPosition) extends Player
}
