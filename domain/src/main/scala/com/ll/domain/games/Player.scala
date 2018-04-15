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
    case class HumanPlayer(user: User, position: PlayerPosition[Riichi]) extends Player[Riichi] {
      def nickName = user.nickname
    }

    case class AIPlayer(ai: AIType[Riichi], position: PlayerPosition[Riichi]) extends Player[Riichi] {
      def nickName = ai.getClass.getName
    }
  }
}
