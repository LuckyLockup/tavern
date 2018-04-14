package com.ll.domain.games

import com.ll.domain.ai.AIType
import com.ll.domain.auth.{User, UserId}

sealed trait Player {
  def nickName: String
  def position: PlayerPosition
}


object Player{
  case class HumanPlayer(user: User, position: PlayerPosition) extends Player {
    def nickName = user.nickname
  }

  case class AIPlayer(ai: AIType, position: PlayerPosition) extends Player {
    def nickName = ai.getClass.getName
  }
}
