package com.ll.domain.games

import com.ll.domain.auth.UserId

sealed trait Player {
  def nickName: String
}

case class User(nickName: String, userId: UserId) extends Player

case class AI(nickName: String) extends Player