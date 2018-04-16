package com.ll.domain.games

import com.ll.domain.ai.AIType
import com.ll.domain.auth.User
import com.ll.domain.games.GameType.Riichi
import com.ll.domain.games.position.PlayerPosition

sealed trait Player[GT <: GameType] {
  def nickName: String
  def position: PlayerPosition[GT]
}


object Player {
  object Riichi {
    case class HumanPlayer[GT <: GameType](user: User, position: PlayerPosition[GT]) extends Player[GT] {
      def nickName = user.nickname
    }

    case class AIPlayer[GT <: GameType](ai: AIType[GT], position: PlayerPosition[GT]) extends Player[GT] {
      def nickName = ai.getClass.getName
    }
  }
}
